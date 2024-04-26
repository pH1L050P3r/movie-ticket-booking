package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class App {

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
      if (port == 8081) {
        ActorRef<BookingRegistry.Command> bookingRegistryActor = context.spawn(
          BookingRegistry.create(),
          "BookingRegistry"
        );
        BookingRoutes bookingRoutes = new BookingRoutes(
          context.getSystem(),
          bookingRegistryActor
        );
        startHttpServer(bookingRoutes.urlRoute(), context.getSystem());
      }

      sharding.init(
        Entity.of(
          SimpleCounter.TypeKey,
          entityContext -> SimpleCounter.create()
        )
      );

      EntityRef<SimpleCounter.Command> ref2 = sharding.entityRefFor(
        SimpleCounter.TypeKey,
        "Counter1"
      );
      ref2.tell(new SimpleCounter.Increment());

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

    //ActorSystem<Void> system =
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
}
