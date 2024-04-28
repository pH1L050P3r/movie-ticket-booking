package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Routers;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import com.example.requestprocessor.RequestProcessingActor;
import com.example.show.ShowActor;
import com.example.theatre.TheatreActor;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  private static final Logger log = LoggerFactory.getLogger(App.class);
  private static final int NO_OF_WORKER = 50;
  static ServiceKey<RequestProcessingActor.Command> serviceKey = ServiceKey.create(
    RequestProcessingActor.Command.class,
    "worker"
  );

  public static void main(String[] args) {
    if (args.length == 0) {
      startup(8081);
      startup(8083);
      startup(8084);
    } else Arrays.stream(args).map(Integer::parseInt).forEach(App::startup);
  }

  private static Behavior<Void> rootBehavior(int port) {
    return Behaviors.setup(context -> {
      final ClusterSharding sharding = ClusterSharding.get(context.getSystem());
      sharding.init(
        Entity.of(TheatreActor.TypeKey, entityContext -> TheatreActor.create())
      );
      sharding.init(
        Entity.of(ShowActor.TypeKey, entityContext -> ShowActor.create())
      );
      if (port == 8081) {
        ActorRef<RequestProcessingActor.Command> requestProcessor = context.spawn(
          Routers.group(serviceKey),
          "worker-group"
        );
        ActorRef<BookingRegistry.Command> bookingRegistryActor = context.spawn(
          BookingRegistry.create(requestProcessor),
          "BookingRegistry"
        );
        BookingRoutes bookingRoutes = new BookingRoutes(
          context.getSystem(),
          bookingRegistryActor
        );
        loadActorsFromFile(sharding);
        startHttpServer(bookingRoutes.urlRoute(), context.getSystem());
      }
      Set<Long> showIds = getShowsIdList();
      Set<Long> theatreIds = getTheatreList();

      for (int i = 0; i < NO_OF_WORKER; i++) {
        // spawns 50 worker threads and add to group router
        ActorRef<RequestProcessingActor.Command> worker = context.spawn(
          RequestProcessingActor.create(showIds, theatreIds, sharding),
          "worker-" + UUID.randomUUID().toString()
        );
        context
          .getSystem()
          .receptionist()
          .tell(Receptionist.register(serviceKey, worker));
      }

      return Behaviors.empty();
    });
  }

  private static void startup(int port) {
    int newPort = port;
    if (port == 8081) {
      newPort = 22593;
    }
    Map<String, Object> overrides = new HashMap<>();
    overrides.put("akka.remote.artery.canonical.port", newPort);
    Config config = ConfigFactory
      .parseMap(overrides)
      .withFallback(ConfigFactory.load());
    ActorSystem.create(rootBehavior(port), "ClusterSystem", config);
  }

  static void startHttpServer(Route route, ActorSystem<?> system) {
    CompletionStage<ServerBinding> futureBinding = Http
      .get(system)
      .newServerAt("0.0.0.0", 8081)
      .bind(route);

    futureBinding.whenComplete((binding, exception) -> {
      if (binding != null) {
        InetSocketAddress address = binding.localAddress();
        system
          .log()
          .info(
            "Server online at http://{}:{}/",
            address.getHostString(),
            address.getPort()
          );
      } else {
        system
          .log()
          .error("Failed to bind HTTP endpoint, terminating system", exception);
        system.terminate();
      }
    });
  }

  private static void loadActorsFromFile(ClusterSharding sharding) {
    String csvFilePathTheatres = "theatres.csv";
    String csvFilePathShows = "shows.csv";
    try (
      CSVReader csvReaderTheater = new CSVReaderBuilder(
        new FileReader(csvFilePathTheatres)
      )
        .withSkipLines(1)
        .build();
      CSVReader csvReaderShow = new CSVReaderBuilder(
        new FileReader(csvFilePathShows)
      )
        .withSkipLines(1)
        .build()
    ) {
      List<String[]> theatreRecords = csvReaderTheater.readAll();
      for (String[] record : theatreRecords) {
        Long id = Long.valueOf(record[0]);
        String name = record[1];
        String location = record[2];
        EntityRef<TheatreActor.Command> theatreActor = sharding.entityRefFor(
          TheatreActor.TypeKey,
          "Theater-" + Long.toString(id)
        );
        theatreActor.tell(new TheatreActor.Initialize(id, name, location));
      }

      List<String[]> showRecords = csvReaderShow.readAll();
      for (String[] record : showRecords) {
        Long id = Long.valueOf(record[0]);
        Long theatreId = Long.valueOf(record[1]);
        String title = record[2];
        Long price = Long.valueOf(record[3]);
        Long seatsAvailable = Long.valueOf(record[4]);
        EntityRef<TheatreActor.Command> theatreActor = sharding.entityRefFor(
          TheatreActor.TypeKey,
          "Theater-" + Long.toString(theatreId)
        );
        EntityRef<ShowActor.Command> showActor = sharding.entityRefFor(
          ShowActor.TypeKey,
          "Show-" + Long.toString(id)
        );
        showActor.tell(
          new ShowActor.Initialize(id, title, price, seatsAvailable, theatreId)
        );
        theatreActor.tell(new TheatreActor.UpdateShows(id, showActor));
      }
    } catch (Exception e) {
      log.error("Error loading data from CSV: " + e.getMessage(), e);
    }
    log.info("Data Loaded Successfully");
  }

  private static Set<Long> getShowsIdList() {
    String csvFilePathTheatres = "theatres.csv";
    String csvFilePathShows = "shows.csv";
    Set<Long> showIdList = new HashSet<>();
    try (
      CSVReader csvReaderTheater = new CSVReaderBuilder(
        new FileReader(csvFilePathTheatres)
      )
        .withSkipLines(1)
        .build();
      CSVReader csvReaderShow = new CSVReaderBuilder(
        new FileReader(csvFilePathShows)
      )
        .withSkipLines(1)
        .build()
    ) {
      List<String[]> showRecords = csvReaderShow.readAll();

      for (String[] record : showRecords) {
        Long id = Long.valueOf(record[0]);
        showIdList.add(id);
      }
    } catch (Exception e) {
      log.error("Error loading data from CSV: " + e.getMessage(), e);
    }
    return showIdList;
  }

  private static Set<Long> getTheatreList() {
    String csvFilePathTheatres = "theatres.csv";
    String csvFilePathShows = "shows.csv";
    Set<Long> theatreIdList = new HashSet<>();
    try (
      CSVReader csvReaderTheater = new CSVReaderBuilder(
        new FileReader(csvFilePathTheatres)
      )
        .withSkipLines(1)
        .build();
      CSVReader csvReaderShow = new CSVReaderBuilder(
        new FileReader(csvFilePathShows)
      )
        .withSkipLines(1)
        .build()
    ) {
      List<String[]> theatreRecords = csvReaderTheater.readAll();
      for (String[] record : theatreRecords) {
        Long id = Long.valueOf(record[0]);
        theatreIdList.add(id);
      }
    } catch (Exception e) {
      log.error("Error loading data from CSV: " + e.getMessage(), e);
    }
    return theatreIdList;
  }
}
