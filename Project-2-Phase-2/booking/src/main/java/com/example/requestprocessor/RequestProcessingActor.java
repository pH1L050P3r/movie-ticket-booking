package com.example.requestprocessor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
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
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestProcessingActor
  extends AbstractBehavior<RequestProcessingActor.Command> {

  private final Duration askTimeout;
  private final Scheduler scheduler;
  private final Http http;
  private final Map<Long, ActorRef<ShowActor.Command>> showMap;
  private final Map<Long, ActorRef<TheatreActor.Command>> theatreMap;
  private static final Logger log = LoggerFactory.getLogger(
    RequestProcessingActor.class
  );

  public static Behavior<Command> create(
    Map<Long, ActorRef<ShowActor.Command>> showMap,
    Map<Long, ActorRef<TheatreActor.Command>> theatreMap
  ) {
    return Behaviors.setup(context ->
      new RequestProcessingActor(context, showMap, theatreMap)
    );
  }

  private RequestProcessingActor(
    ActorContext<Command> context,
    Map<Long, ActorRef<ShowActor.Command>> showMap,
    Map<Long, ActorRef<TheatreActor.Command>> theatreMap
  ) {
    super(context);
    this.showMap = showMap;
    this.theatreMap = theatreMap;
    this.askTimeout =
      context
        .getSystem()
        .settings()
        .config()
        .getDuration("my-app.routes.ask-timeout");
    this.scheduler = getContext().getSystem().scheduler();
    this.http = Http.get(getContext().getSystem());
  }

  public interface Command {}

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
    ActorRef<ShowActor.Command> showActor = showMap.get(command.showId());
    if (showActor != null) {
      Show show = RequestProcessingUtils.getShowFromShowActor(
        showActor,
        askTimeout,
        scheduler
      );
      command
        .replyTo()
        .tell(new BookingRegistry.GetShowResponse(show, StatusCodes.OK, ""));
    } else {
      command
        .replyTo()
        .tell(
          new BookingRegistry.GetShowResponse(
            null,
            StatusCodes.NOT_FOUND,
            "Show not found"
          )
        );
    }
    return this;
  }

  private Behavior<Command> onGetTheatre(GetTheatreRequestProcess message) {
    ActorRef<TheatreActor.Command> theatreActor = theatreMap.get(
      message.theatreId()
    );
    if (theatreActor != null) {
      Theatre theatre = RequestProcessingUtils.getTheatreFromTheatreActor(
        theatreActor,
        askTimeout,
        scheduler
      );
      message
        .replyTo()
        .tell(
          new BookingRegistry.GetTheatreResponse(theatre, StatusCodes.OK, "")
        );
    } else {
      message
        .replyTo()
        .tell(
          new BookingRegistry.GetTheatreResponse(
            null,
            StatusCodes.NOT_FOUND,
            "Theatre not Found"
          )
        );
    }
    return this;
  }

  private Behavior<Command> onGetAllTheatres(
    GetAllTheatresRequestProcess message
  ) {
    Collection<ActorRef<TheatreActor.Command>> theatresActors = theatreMap.values();
    List<Theatre> theatres = RequestProcessingUtils.getTheatreListFromTheatreActorList(
      theatresActors,
      askTimeout,
      scheduler
    );
    message
      .replyTo()
      .tell(
        new BookingRegistry.GetAllTheatresResponse(theatres, StatusCodes.OK, "")
      );
    return this;
  }

  private Behavior<Command> onGetTheatreAllShows(
    GetTheatreAllShowsRequestProcess message
  ) {
    ActorRef<TheatreActor.Command> theatreActor = theatreMap.get(
      message.theatreId()
    );
    if (theatreActor != null) {
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
            StatusCodes.OK,
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
            StatusCodes.NOT_FOUND,
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

    for (ActorRef<ShowActor.Command> showActor : showMap.values()) {
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
        log.info(response.bookings().toString());
        bookings.addAll(response.bookings());
      });
    }
    message
      .replyTo()
      .tell(
        new BookingRegistry.GetUserAllBookingsResponse(
          bookings,
          StatusCodes.OK,
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

    ActorRef<ShowActor.Command> showActor = showMap.get(showId);

    if (showActor == null/* || !UserService.isUserExist(userId, http)*/) {
      message
        .replyTo()
        .tell(
          new BookingRegistry.CreateBookingResponse(
            null,
            StatusCodes.BAD_REQUEST,
            "User or Show does not exists"
          )
        );
      return this;
    }

    Show show = RequestProcessingUtils.getShowFromShowActor(
      showActor,
      askTimeout,
      scheduler
    );
    Long amount = show.price() * seatsBooked;
    String paymentResponse = PaymentService.payment(userId, amount, http);
    if (
      show.seatsAvailable() < seatsBooked ||
      !paymentResponse.equalsIgnoreCase("SUCCESS")
    ) {
      message
        .replyTo()
        .tell(
          new BookingRegistry.CreateBookingResponse(
            null,
            StatusCodes.BAD_REQUEST,
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
              StatusCodes.BAD_REQUEST,
              "Seats not Available"
            )
          );
        log.info("Seats : USER ID :" + userId.toString());
      } else {
        message.replyTo.tell(
          new BookingRegistry.CreateBookingResponse(
            response,
            StatusCodes.OK,
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
            StatusCodes.NOT_FOUND,
            "User does not exists"
          )
        );
      return this;
    }

    DeleteBookingResponse response = RequestProcessingUtils.deleteAllUserBookings(
      showMap.values(),
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
          StatusCodes.OK,
          "Payment Status :" + status
        )
      );

    return this;
  }

  private Behavior<Command> onDeleteUserShowBookings(
    DeleteUserShowBookingsRequestProcess message
  ) {
    Long userId = message.userId();
    ActorRef<ShowActor.Command> show = showMap.get(message.showId());
    if (show == null || !UserService.isUserExist(userId, http)) {
      message
        .replyTo()
        .tell(
          new BookingRegistry.DeleteUserShowBookingsResponse(
            StatusCodes.NOT_FOUND,
            "User or Show does not exists"
          )
        );
      return this;
    }

    Collection<ActorRef<ShowActor.Command>> shows = new HashSet<>();
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
          StatusCodes.OK,
          "Payment Status :" + status
        )
      );

    return this;
  }

  private Behavior<Command> onDeleteAllBookings(
    DeleteAllBookingsProcess message
  ) {
    DeleteBookingResponse res = RequestProcessingUtils.deleteAllBookings(
      showMap.values(),
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
      .tell(new BookingRegistry.DeleteAllBookingsResponse(StatusCodes.OK, ""));
    return this;
  }
}