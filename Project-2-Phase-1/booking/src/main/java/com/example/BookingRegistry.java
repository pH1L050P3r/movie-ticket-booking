package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.http.javadsl.model.StatusCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BookingRegistry extends AbstractBehavior<BookingRegistry.Command> {

  private Map<Long, ActorRef<ShowActor.Command>> showsMap = new HashMap<>();
  private Map<Long, ActorRef<TheatreActor.Command>> theatreMap = new HashMap<>();
  private static final Logger log = LoggerFactory.getLogger(
    BookingRegistry.class
  );

  sealed interface Command {}

  public static final record GetShowRequest(
    ActorRef<GetShowResponse> retplyTo,
    Long id
  )
    implements Command {}

  public static final record GetTheatreRequest(
    ActorRef<GetTheatreResponse> replyTo,
    Long id
  )
    implements Command {}

  public static final record GetAllTheatresRequest(
    ActorRef<GetAllTheatresResponse> replyTo
  )
    implements Command {}

  public static final record GetTheatreAllShowsRequest(
    ActorRef<GetTheatreAllShowsResponse> replyTo,
    Long threatreId
  )
    implements Command {}

  public static final record GetUserAllBookingsRequest(
    ActorRef<GetUserAllBookingsResponse> replyTo,
    Long userId
  )
    implements Command {}

  public static final record CreateBookingRequest(
    ActorRef<CreateBookingResponse> replyTo,
    CreateBookingRequestBody requestBody
  )
    implements Command {}

  public static final record DeleteUserAllBookingsRequest(
    ActorRef<DeleteUserAllBookingsResponse> replyTo,
    Long userId
  )
    implements Command {}

  public static final record GetShowResponse(
    ShowActor.Show body,
    StatusCode statusCode,
    String message
  ) {}

  public static final record GetTheatreResponse(
    TheatreActor.Theatre body,
    StatusCode statusCode,
    String message
  ) {}

  public static final record GetAllTheatresResponse(
    List<TheatreActor.Theatre> body,
    StatusCode statusCode,
    String message
  ) {}

  public static final record GetTheatreAllShowsResponse(
    List<ShowActor.Show> body,
    StatusCode statusCode,
    String message
  ) {}

  public static final record GetUserAllBookingsResponse(
    List<ShowActor.Booking> body,
    StatusCode statusCode,
    String message
  ) {}

  public static final record CreateBookingResponse(
    CreateBookingResponseBody body,
    StatusCode statusCode,
    String message
  ) {}

  public static final record DeleteUserAllBookingsResponse(
    StatusCode statusCode,
    String message
  ) {}

  public static final record CreateBookingRequestBody(
    @JsonProperty("show_id") Long showId,
    @JsonProperty("user_id") Long userId,
    @JsonProperty("seats_booked") Long seatsBooked
  ) {}

  public static final record CreateBookingResponseBody(
    @JsonProperty("id") Long id,
    @JsonProperty("show_id") Long showId,
    @JsonProperty("user_id") Long userId,
    @JsonProperty("seats_booked") Long seatsBooked
  ) {}

  public static Behavior<BookingRegistry.Command> create() {
    return Behaviors.setup(BookingRegistry::new);
  }

  private BookingRegistry(ActorContext<BookingRegistry.Command> context) {
    super(context);
    loadActorsFromFile(context);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
      .onMessage(GetShowRequest.class, this::onGetShow)
      .onMessage(GetTheatreRequest.class, this::onGetTheatre)
      .onMessage(GetTheatreAllShowsRequest.class, this::onGetTheatreAllShows)
      .onMessage(CreateBookingRequest.class, this::onCreateBooking)
      .onMessage(GetUserAllBookingsRequest.class, this::onGetUserAllBookings)
      .onMessage(GetAllTheatresRequest.class, this::onGetAllTheatres)
      .onMessage(
        DeleteUserAllBookingsRequest.class,
        this::onDeleteUserAllBookings
      )
      .build();
  }

  private Behavior<Command> onGetShow(GetShowRequest command) {
    getContext()
      .spawn(RequestProcessingActor.create(), "actor")
      .tell(
        new RequestProcessingActor.GetShowRequestProcess(
          command.retplyTo(),
          command.id(),
          Collections.unmodifiableMap(showsMap)
        )
      );
    return this;
  }

  private Behavior<Command> onGetTheatre(GetTheatreRequest command) {
    getContext()
      .spawn(RequestProcessingActor.create(), "processing-actor")
      .tell(
        new RequestProcessingActor.GetTheatreRequestProcess(
          command.replyTo(),
          command.id(),
          Collections.unmodifiableMap(theatreMap)
        )
      );
    return this;
  }

  private Behavior<Command> onGetAllTheatres(GetAllTheatresRequest command) {
    getContext()
      .spawn(RequestProcessingActor.create(), "processing-actor")
      .tell(
        new RequestProcessingActor.GetAllTheatresRequestProcess(
          command.replyTo(),
          Collections.unmodifiableMap(theatreMap)
        )
      );
    return this;
  }

  private Behavior<Command> onGetTheatreAllShows(
    GetTheatreAllShowsRequest command
  ) {
    getContext()
      .spawn(RequestProcessingActor.create(), "processing-actor")
      .tell(
        new RequestProcessingActor.GetTheatreAllShowsRequestProcess(
          command.replyTo(),
          command.threatreId(),
          Collections.unmodifiableMap(theatreMap)
        )
      );
    return this;
  }

  private Behavior<Command> onGetUserAllBookings(
    GetUserAllBookingsRequest command
  ) {
    getContext()
      .spawn(RequestProcessingActor.create(), "processing-actor")
      .tell(
        new RequestProcessingActor.GetUserAllBookingsRequestProcess(
          command.replyTo(),
          command.userId(),
          Collections.unmodifiableMap(showsMap)
        )
      );
    return this;
  }

  private Behavior<Command> onDeleteUserAllBookings(
    DeleteUserAllBookingsRequest command
  ) {
    getContext()
      .spawn(RequestProcessingActor.create(), "processing-actor")
      .tell(
        new RequestProcessingActor.DeleteUserAllBookingsRequestProcess(
          command.replyTo,
          Collections.unmodifiableMap(showsMap),
          command.userId()
        )
      );
    return this;
  }

  private Behavior<Command> onCreateBooking(CreateBookingRequest message) {
    getContext()
      .spawn(RequestProcessingActor.create(), "processing-actor")
      .tell(
        new RequestProcessingActor.CreateBookingRequestProcess(
          message.replyTo(),
          Collections.unmodifiableMap(showsMap),
          message.requestBody()
        )
      );
    return this;
  }

  private void loadActorsFromFile(
    ActorContext<BookingRegistry.Command> context
  ) {
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
        ActorRef<TheatreActor.Command> theatreActor = context.spawn(
          TheatreActor.create(id, name, location),
          "Theater-" + Long.toString(id)
        );
        theatreMap.put(id, theatreActor);
      }

      List<String[]> showRecords = csvReaderShow.readAll();
      for (String[] record : showRecords) {
        Long id = Long.valueOf(record[0]);
        Long theatreId = Long.valueOf(record[1]);
        ActorRef<TheatreActor.Command> theatreActor = theatreMap.get(theatreId);
        String title = record[2];
        Long price = Long.valueOf(record[3]);
        Long seatsAvailable = Long.valueOf(record[4]);
        ActorRef<ShowActor.Command> showActor = context.spawn(
          ShowActor.create(
            id,
            title,
            price,
            seatsAvailable,
            theatreId,
            theatreActor
          ),
          "Show-" + Long.toString(id)
        );
        theatreActor.tell(new TheatreActor.UpdateShows(id, showActor));
        showsMap.put(id, showActor);
      }
    } catch (Exception e) {
      System.err.println("Error loading data from CSV: " + e.getMessage());
    }
    log.info("Data Loaded Successfully");
  }
}
