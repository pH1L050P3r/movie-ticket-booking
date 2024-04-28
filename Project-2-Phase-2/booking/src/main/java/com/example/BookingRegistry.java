package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import com.example.requestprocessor.RequestProcessingActor;
import com.example.show.ShowActor;
import com.example.theatre.TheatreActor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class BookingRegistry extends AbstractBehavior<BookingRegistry.Command> {

  ServiceKey<RequestProcessingActor.Command> serviceKey = ServiceKey.create(
    RequestProcessingActor.Command.class,
    "worker"
  );
  ActorRef<RequestProcessingActor.Command> requestProcessor;

  public static final EntityTypeKey<Command> TypeKey = EntityTypeKey.create(
    BookingRegistry.Command.class,
    "BookingRegistryEntity"
  );

  sealed interface Command extends CborSerializable {}

  public static final record GetShowRequest(
    ActorRef<GetShowResponse> replyTo,
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
    Long theatreId
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

  public static final record DeleteUserShowBookingsRequest(
    ActorRef<DeleteUserShowBookingsResponse> replyTo,
    Long userId,
    Long showId
  )
    implements Command {}

  public static final record DeleteAllBookingsRequest(
    ActorRef<DeleteAllBookingsResponse> replyTo
  )
    implements Command {}

  public static final record GetShowResponse(
    ShowActor.Show body,
    int statusCode,
    String message
  )
    implements Command {}

  public static final record GetTheatreResponse(
    TheatreActor.Theatre body,
    int statusCode,
    String message
  )
    implements Command {}

  public static final record GetAllTheatresResponse(
    List<TheatreActor.Theatre> body,
    int statusCode,
    String message
  )
    implements Command {}

  public static final record GetTheatreAllShowsResponse(
    List<ShowActor.Show> body,
    int statusCode,
    String message
  )
    implements Command {}

  public static final record GetUserAllBookingsResponse(
    List<ShowActor.Booking> body,
    int statusCode,
    String message
  )
    implements Command {}

  public static final record CreateBookingResponse(
    ShowActor.Booking body,
    int statusCode,
    String message
  )
    implements Command {}

  public static final record DeleteUserAllBookingsResponse(
    int statusCode,
    String message
  )
    implements Command {}

  public static final record DeleteUserShowBookingsResponse(
    int statusCode,
    String message
  )
    implements Command {}

  public static final record DeleteAllBookingsResponse(
    int statusCode,
    String message
  )
    implements Command {}

  public static final record CreateBookingRequestBody(
    @JsonProperty("show_id") Long showId,
    @JsonProperty("user_id") Long userId,
    @JsonProperty("seats_booked") Long seatsBooked
  )
    implements Command {}

  public static final record CreateBookingResponseBody(
    @JsonProperty("id") Long id,
    @JsonProperty("show_id") Long showId,
    @JsonProperty("user_id") Long userId,
    @JsonProperty("seats_booked") Long seatsBooked
  )
    implements Command {}

  public static Behavior<BookingRegistry.Command> create(
    ActorRef<RequestProcessingActor.Command> group
  ) {
    return Behaviors.setup(context -> new BookingRegistry(context, group));
  }

  private BookingRegistry(
    ActorContext<BookingRegistry.Command> context,
    ActorRef<RequestProcessingActor.Command> group
  ) {
    super(context);
    requestProcessor = group;
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
      .onMessage(
        DeleteUserShowBookingsRequest.class,
        this::onDeleteUserShowBookings
      )
      .onMessage(DeleteAllBookingsRequest.class, this::onDeleteAllBookings)
      .build();
  }

  private Behavior<Command> onGetShow(GetShowRequest command) {
    requestProcessor.tell(
      new RequestProcessingActor.GetShowRequestProcess(
        command.replyTo(),
        command.id()
      )
    );
    return this;
  }

  private Behavior<Command> onGetTheatre(GetTheatreRequest command) {
    requestProcessor.tell(
      new RequestProcessingActor.GetTheatreRequestProcess(
        command.replyTo(),
        command.id()
      )
    );
    return this;
  }

  private Behavior<Command> onGetAllTheatres(GetAllTheatresRequest command) {
    requestProcessor.tell(
      new RequestProcessingActor.GetAllTheatresRequestProcess(command.replyTo())
    );
    return this;
  }

  private Behavior<Command> onGetTheatreAllShows(
    GetTheatreAllShowsRequest command
  ) {
    requestProcessor.tell(
      new RequestProcessingActor.GetTheatreAllShowsRequestProcess(
        command.replyTo(),
        command.theatreId()
      )
    );
    return this;
  }

  private Behavior<Command> onGetUserAllBookings(
    GetUserAllBookingsRequest command
  ) {
    requestProcessor.tell(
      new RequestProcessingActor.GetUserAllBookingsRequestProcess(
        command.replyTo(),
        command.userId()
      )
    );
    return this;
  }

  private Behavior<Command> onDeleteUserAllBookings(
    DeleteUserAllBookingsRequest command
  ) {
    requestProcessor.tell(
      new RequestProcessingActor.DeleteUserAllBookingsRequestProcess(
        command.replyTo(),
        command.userId()
      )
    );
    return this;
  }

  private Behavior<Command> onDeleteUserShowBookings(
    DeleteUserShowBookingsRequest command
  ) {
    requestProcessor.tell(
      new RequestProcessingActor.DeleteUserShowBookingsRequestProcess(
        command.replyTo(),
        command.userId(),
        command.showId()
      )
    );
    return this;
  }

  private Behavior<Command> onDeleteAllBookings(
    DeleteAllBookingsRequest command
  ) {
    requestProcessor.tell(
      new RequestProcessingActor.DeleteAllBookingsProcess(command.replyTo())
    );
    return this;
  }

  private Behavior<Command> onCreateBooking(CreateBookingRequest message) {
    requestProcessor.tell(
      new RequestProcessingActor.CreateBookingRequestProcess(
        message.replyTo(),
        message.requestBody()
      )
    );
    return this;
  }
}
