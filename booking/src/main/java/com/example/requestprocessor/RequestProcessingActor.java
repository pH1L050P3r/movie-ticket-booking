package com.example.requestprocessor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.serialization.jackson.CborSerializable;
import com.example.BookingRegistry;
import com.example.services.PaymentService;
import com.example.services.UserService;
import com.example.show.ShowActor;
import com.example.show.ShowActor.DeleteBookingResponse;
import com.example.show.ShowActor.Show;
import com.example.theatre.TheatreActor;
import com.example.theatre.TheatreActor.Theatre;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestProcessingActor
  extends AbstractBehavior<RequestProcessingActor.Command> {

  public final Duration askTimeout;
  public final Scheduler scheduler;
  public final Http http;
  public final Set<Long> showIdList;
  public final Set<Long> theatreIdList;
  public final ClusterSharding sharding;
  public static final Logger log = LoggerFactory.getLogger(
    RequestProcessingActor.class
  );

  public static Behavior<Command> create(
    Set<Long> showIdList,
    Set<Long> theatreIdList,
    ClusterSharding sharding
  ) {
    return Behaviors.setup(context ->
      new RequestProcessingActor(context, showIdList, theatreIdList, sharding)
    );
  }

  private RequestProcessingActor(
    ActorContext<Command> context,
    Set<Long> showIdList,
    Set<Long> theatreIdList,
    ClusterSharding sharding
  ) {
    super(context);
    this.showIdList = showIdList;
    this.theatreIdList = theatreIdList;
    this.sharding = sharding;
    this.askTimeout =
      context
        .getSystem()
        .settings()
        .config()
        .getDuration("my-app.routes.ask-timeout");
    this.scheduler = getContext().getSystem().scheduler();
    this.http = Http.get(getContext().getSystem());
  }

  public interface Command extends CborSerializable {}

  public static final record GetShowRequestProcess(
    ActorRef<BookingRegistry.GetShowResponse> replyTo,
    Long showId
  )
    implements Command {}

  public static final record GetTheatreRequestProcess(
    ActorRef<BookingRegistry.GetTheatreResponse> replyTo,
    Long theatreId
  )
    implements Command {}

  public static final record GetAllTheatresRequestProcess(
    ActorRef<BookingRegistry.GetAllTheatresResponse> replyTo
  )
    implements Command {}

  public static final record GetTheatreAllShowsRequestProcess(
    ActorRef<BookingRegistry.GetTheatreAllShowsResponse> replyTo,
    Long theatreId
  )
    implements Command {}

  public static final record CreateBookingRequestProcess(
    ActorRef<BookingRegistry.CreateBookingResponse> replyTo,
    BookingRegistry.CreateBookingRequestBody requestBody
  )
    implements Command {}

  public static final record GetUserAllBookingsRequestProcess(
    ActorRef<BookingRegistry.GetUserAllBookingsResponse> replyTo,
    Long userId
  )
    implements Command {}

  public static final record DeleteUserAllBookingsRequestProcess(
    ActorRef<BookingRegistry.DeleteUserAllBookingsResponse> replyTo,
    Long userId
  )
    implements Command {}

  public static final record DeleteUserShowBookingsRequestProcess(
    ActorRef<BookingRegistry.DeleteUserShowBookingsResponse> replyTo,
    Long userId,
    Long showId
  )
    implements Command {}

  public static final record DeleteAllBookingsProcess(
    ActorRef<BookingRegistry.DeleteAllBookingsResponse> replyTo
  )
    implements Command {}

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
      .onMessage(GetShowRequestProcess.class, this::onGetShow)
      .onMessage(GetTheatreRequestProcess.class, this::onGetTheatre)
      .onMessage(
        GetTheatreAllShowsRequestProcess.class,
        this::onGetTheatreAllShows
      )
      .onMessage(CreateBookingRequestProcess.class, this::onCreateBooking)
      .onMessage(
        GetUserAllBookingsRequestProcess.class,
        this::onGetUserAllBookings
      )
      .onMessage(GetAllTheatresRequestProcess.class, this::onGetAllTheatres)
      .onMessage(
        DeleteUserAllBookingsRequestProcess.class,
        this::onDeleteUserAllBookings
      )
      .onMessage(
        DeleteUserShowBookingsRequestProcess.class,
        this::onDeleteUserShowBookings
      )
      .onMessage(DeleteAllBookingsProcess.class, this::onDeleteAllBookings)
      .build();
  }

  private Behavior<Command> onGetShow(GetShowRequestProcess command) {
    if (showIdList.contains(command.showId())) {
      EntityRef<ShowActor.Command> showActor = sharding.entityRefFor(
        ShowActor.TypeKey,
        "Show-" + Long.toString(command.showId())
      );
      Show show = RequestProcessingUtils.getShowFromShowActor(
        showActor,
        askTimeout,
        scheduler
      );
      command
        .replyTo()
        .tell(
          new BookingRegistry.GetShowResponse(
            show,
            StatusCodes.OK.intValue(),
            ""
          )
        );
    } else {
      command
        .replyTo()
        .tell(
          new BookingRegistry.GetShowResponse(
            null,
            StatusCodes.NOT_FOUND.intValue(),
            "Show not found"
          )
        );
    }
    return this;
  }

  private Behavior<Command> onGetTheatre(GetTheatreRequestProcess message) {
    if (theatreIdList.contains(message.theatreId())) {
      EntityRef<TheatreActor.Command> theatreActor = sharding.entityRefFor(
        TheatreActor.TypeKey,
        "Theater-" + Long.toString(message.theatreId())
      );
      Theatre theatre = RequestProcessingUtils.getTheatreFromTheatreActor(
        theatreActor,
        askTimeout,
        scheduler
      );
      message
        .replyTo()
        .tell(
          new BookingRegistry.GetTheatreResponse(
            theatre,
            StatusCodes.OK.intValue(),
            ""
          )
        );
    } else {
      message
        .replyTo()
        .tell(
          new BookingRegistry.GetTheatreResponse(
            null,
            StatusCodes.NOT_FOUND.intValue(),
            "Theatre not Found"
          )
        );
    }
    return this;
  }

  private Behavior<Command> onGetAllTheatres(
    GetAllTheatresRequestProcess message
  ) {
    Collection<EntityRef<TheatreActor.Command>> theatresActors = new ArrayList<EntityRef<TheatreActor.Command>>();
    for (Long theatreId : theatreIdList) {
      theatresActors.add(
        sharding.entityRefFor(
          TheatreActor.TypeKey,
          "Theater-" + Long.toString(theatreId)
        )
      );
    }
    List<Theatre> theatres = RequestProcessingUtils.getTheatreListFromTheatreActorList(
      theatresActors,
      askTimeout,
      scheduler
    );
    message
      .replyTo()
      .tell(
        new BookingRegistry.GetAllTheatresResponse(
          theatres,
          StatusCodes.OK.intValue(),
          ""
        )
      );
    return this;
  }

  private Behavior<Command> onGetTheatreAllShows(
    GetTheatreAllShowsRequestProcess message
  ) {
    if (theatreIdList.contains(message.theatreId())) {
      EntityRef<TheatreActor.Command> theatreActor = sharding.entityRefFor(
        TheatreActor.TypeKey,
        "Theater-" + Long.toString(message.theatreId())
      );
      List<Show> shows = RequestProcessingUtils.getTheatreAllShows(
        theatreActor,
        askTimeout,
        scheduler
      );
      message
        .replyTo()
        .tell(
          new BookingRegistry.GetTheatreAllShowsResponse(
            shows,
            StatusCodes.OK.intValue(),
            ""
          )
        );
    } else {
      List<ShowActor.Show> emptyList = new ArrayList<>();
      message
        .replyTo()
        .tell(
          new BookingRegistry.GetTheatreAllShowsResponse(
            emptyList,
            StatusCodes.NOT_FOUND.intValue(),
            "Theatre not exists"
          )
        );
    }
    return this;
  }

  private Behavior<Command> onGetUserAllBookings(
    GetUserAllBookingsRequestProcess message
  ) {
    Long userId = message.userId();
    List<ShowActor.Booking> bookings = new ArrayList<>();
    List<CompletionStage<ShowActor.Bookings>> completionStages = new ArrayList<>();

    for (Long showId : showIdList) {
      EntityRef<ShowActor.Command> showActor = sharding.entityRefFor(
        ShowActor.TypeKey,
        "Show-" + Long.toString(showId)
      );
      CompletionStage<ShowActor.Bookings> completion = AskPattern.ask(
        showActor,
        ref -> new ShowActor.GetUserBookings(ref, userId),
        askTimeout,
        scheduler
      );
      completionStages.add(completion);
    }

    for (CompletionStage<ShowActor.Bookings> completion : completionStages) {
      completion.thenAccept(response -> {
        bookings.addAll(response.bookings());
      });
    }
    message
      .replyTo()
      .tell(
        new BookingRegistry.GetUserAllBookingsResponse(
          bookings,
          StatusCodes.OK.intValue(),
          ""
        )
      );
    return this;
  }

  private Behavior<Command> onCreateBooking(
    CreateBookingRequestProcess message
  ) {
    Long showId = message.requestBody.showId();
    Long userId = message.requestBody.userId();
    Long seatsBooked = message.requestBody.seatsBooked();

    if (!showIdList.contains(showId)) {
      message
        .replyTo()
        .tell(
          new BookingRegistry.CreateBookingResponse(
            null,
            StatusCodes.BAD_REQUEST.intValue(),
            "User or Show does not exists"
          )
        );
      return this;
    }
    EntityRef<ShowActor.Command> showActor = sharding.entityRefFor(
      ShowActor.TypeKey,
      "Show-" + Long.toString(showId)
    );
    Show show = RequestProcessingUtils.getShowFromShowActor(
      showActor,
      askTimeout,
      scheduler
    );
    Long amount = show.price * seatsBooked;
    String paymentResponse = PaymentService.payment(userId, amount, http);
    if (
      show.seatsAvailable < seatsBooked ||
      !paymentResponse.equalsIgnoreCase("SUCCESS")
    ) {
      message
        .replyTo()
        .tell(
          new BookingRegistry.CreateBookingResponse(
            null,
            StatusCodes.BAD_REQUEST.intValue(),
            "Payment failed or Seats not available"
          )
        );
      return this;
    }

    // create booking
    CompletionStage<ShowActor.Booking> completion = AskPattern.ask(
      showActor,
      ref -> new ShowActor.CreateShowBooking(ref, userId, seatsBooked),
      askTimeout,
      scheduler
    );
    completion.thenAccept(response -> {
      if (response.id() == -1) {
        PaymentService.refund(userId, amount, http);
        message
          .replyTo()
          .tell(
            new BookingRegistry.CreateBookingResponse(
              null,
              StatusCodes.BAD_REQUEST.intValue(),
              "Seats not Available"
            )
          );
        log.info("Seats : USER ID :" + userId.toString());
      } else {
        message.replyTo.tell(
          new BookingRegistry.CreateBookingResponse(
            response,
            StatusCodes.OK.intValue(),
            ""
          )
        );
      }
    });
    return this;
  }

  private Behavior<Command> onDeleteUserAllBookings(
    DeleteUserAllBookingsRequestProcess message
  ) {
    Long userId = message.userId();
    if (!UserService.isUserExist(userId, http)) {
      message
        .replyTo()
        .tell(
          new BookingRegistry.DeleteUserAllBookingsResponse(
            StatusCodes.NOT_FOUND.intValue(),
            "User does not exists"
          )
        );
      return this;
    }

    Collection<EntityRef<ShowActor.Command>> showActors = new ArrayList<>();
    for (Long showId : showIdList) {
      EntityRef<ShowActor.Command> showActor = sharding.entityRefFor(
        ShowActor.TypeKey,
        "Show-" + Long.toString(showId)
      );
      showActors.add(showActor);
    }

    DeleteBookingResponse response = RequestProcessingUtils.deleteAllUserBookings(
      showActors,
      userId,
      askTimeout,
      scheduler
    );
    Long refundAmount = response.refundUserAmountMap().get(userId);
    String status = PaymentService.refund(userId, refundAmount, http);
    log.info("Payment Status UserId " + userId + " " + status);

    message
      .replyTo()
      .tell(
        new BookingRegistry.DeleteUserAllBookingsResponse(
          StatusCodes.OK.intValue(),
          "Payment Status :" + status
        )
      );

    return this;
  }

  private Behavior<Command> onDeleteUserShowBookings(
    DeleteUserShowBookingsRequestProcess message
  ) {
    Long userId = message.userId();
    if (
      !showIdList.contains(message.showId()) ||
      !UserService.isUserExist(userId, http)
    ) {
      message
        .replyTo()
        .tell(
          new BookingRegistry.DeleteUserShowBookingsResponse(
            StatusCodes.NOT_FOUND.intValue(),
            "User or Show does not exists"
          )
        );
      return this;
    }
    EntityRef<ShowActor.Command> show = sharding.entityRefFor(
      ShowActor.TypeKey,
      "Show-" + Long.toString(message.showId())
    );
    Collection<EntityRef<ShowActor.Command>> shows = new HashSet<>();
    shows.add(show);
    DeleteBookingResponse response = RequestProcessingUtils.deleteAllUserBookings(
      shows,
      userId,
      askTimeout,
      scheduler
    );
    Long refundAmount = response.refundUserAmountMap().get(userId);
    String status = PaymentService.refund(userId, refundAmount, http);
    log.info("Payment Status UserId " + userId + " " + status);
    message
      .replyTo()
      .tell(
        new BookingRegistry.DeleteUserShowBookingsResponse(
          StatusCodes.OK.intValue(),
          "Payment Status :" + status
        )
      );

    return this;
  }

  private Behavior<Command> onDeleteAllBookings(
    DeleteAllBookingsProcess message
  ) {
    Collection<EntityRef<ShowActor.Command>> showActors = new ArrayList<>();
    for (Long showId : showIdList) {
      EntityRef<ShowActor.Command> showActor = sharding.entityRefFor(
        ShowActor.TypeKey,
        "Show-" + Long.toString(showId)
      );
      showActors.add(showActor);
    }
    DeleteBookingResponse res = RequestProcessingUtils.deleteAllBookings(
      showActors,
      askTimeout,
      scheduler
    );

    for (Entry<Long, Long> user : res.refundUserAmountMap().entrySet()) {
      String status = PaymentService.refund(
        user.getKey(),
        user.getValue(),
        http
      );
      log.info(
        "Payment refund Status UserId " + user.getKey() + " : " + status
      );
    }

    message
      .replyTo()
      .tell(
        new BookingRegistry.DeleteAllBookingsResponse(
          StatusCodes.OK.intValue(),
          ""
        )
      );
    return this;
  }
}
/* || !UserService.isUserExist(userId, http)*/
