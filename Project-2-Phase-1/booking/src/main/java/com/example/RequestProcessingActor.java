package com.example;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.BookingRegistry.GetShowResponse;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.http.javadsl.model.StatusCodes;

public class RequestProcessingActor extends AbstractBehavior<RequestProcessingActor.Command> {

    private final Duration askTimeout;
    private final Scheduler scheduler;
    private final static Logger log = LoggerFactory.getLogger(RequestProcessingActor.class);

    public static Behavior<Command> create() {
        return Behaviors.setup(RequestProcessingActor::new);
    }

    private RequestProcessingActor(ActorContext<Command> context) {
        super(context);
        this.askTimeout = Duration.ofSeconds(30);
        this.scheduler = getContext().getSystem().scheduler();
    }

    sealed interface Command {
    }

    public final static record GetShowRequestProcess(
            ActorRef<GetShowResponse> retplyTo, Long showId,
            Map<Long, ActorRef<ShowActor.Command>> showMap) implements Command {
    }

    public final static record GetTheatreRequestProcess(ActorRef<BookingRegistry.GetTheatreResponse> replyTo,
            Long theatreId,
            Map<Long, ActorRef<TheatreActor.Command>> theatreMap) implements Command {
    }

    public final static record GetTheatreAllShowsRequestProcess(
            ActorRef<BookingRegistry.GetTheatreAllShowsResponse> replyTo, Long theatreId,
            Map<Long, ActorRef<TheatreActor.Command>> theatreMap) implements Command {
    }

    public final static record CreateBookingRequestProcess(
            ActorRef<BookingRegistry.CreateBookingResponse> replyTo,
            Map<Long, ActorRef<ShowActor.Command>> showMap, BookingRegistry.CreateBookingRequestBody requestBody)
            implements Command {
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetShowRequestProcess.class, this::onGetShow)
                .onMessage(GetTheatreRequestProcess.class, this::onGetTheatre)
                .onMessage(GetTheatreAllShowsRequestProcess.class, this::onGetTheatreAllShows)
                .onMessage(CreateBookingRequestProcess.class, this::onCreateBooking)
                .build();
    }

    private Behavior<Command> onGetShow(GetShowRequestProcess command) {
        ActorRef<ShowActor.Command> showActor = command.showMap().get(command.showId());
        if (showActor != null) {
            CompletionStage<ShowActor.Show> completion = AskPattern.ask(showActor,
                    ref -> new ShowActor.GetShow(ref), askTimeout, scheduler);
            completion.thenAccept(response -> {
                command.retplyTo().tell(new BookingRegistry.GetShowResponse(response, StatusCodes.OK, ""));
            });
        } else {
            command.retplyTo().tell(new BookingRegistry.GetShowResponse(null, StatusCodes.NOT_FOUND, "Show not found"));
        }
        return Behaviors.stopped();
    }

    private Behavior<Command> onGetTheatre(GetTheatreRequestProcess command) {
        ActorRef<TheatreActor.Command> theatreActor = command.theatreMap().get(command.theatreId());
        if (theatreActor != null) {
            CompletionStage<TheatreActor.Theatre> completion = AskPattern.ask(theatreActor,
                    ref -> new TheatreActor.GetTheatre(ref), askTimeout, scheduler);
            completion.thenAccept(response -> {
                command.replyTo().tell(new BookingRegistry.GetTheatreResponse(response, StatusCodes.OK, ""));
            });
        } else {
            command.replyTo()
                    .tell(new BookingRegistry.GetTheatreResponse(null, StatusCodes.NOT_FOUND, "Theatre not Found"));
        }
        return Behaviors.stopped();
    }

    private Behavior<Command> onGetTheatreAllShows(GetTheatreAllShowsRequestProcess command) {
        ActorRef<TheatreActor.Command> theatreActor = command.theatreMap().get(command.theatreId());
        if (theatreActor != null) {
            CompletionStage<ShowActor.Shows> completion = AskPattern.ask(theatreActor,
                    ref -> new TheatreActor.GetThreatreShows(ref), askTimeout, scheduler);
            completion.thenAccept(response -> {
                command.replyTo()
                        .tell(new BookingRegistry.GetTheatreAllShowsResponse(response.shows(), StatusCodes.OK, ""));
            });
        } else {
            List<ShowActor.Show> empltyList = new ArrayList<>();
            command.replyTo().tell(new BookingRegistry.GetTheatreAllShowsResponse(empltyList, StatusCodes.OK, ""));
        }
        return Behaviors.stopped();
    }

    private Behavior<Command> onCreateBooking(CreateBookingRequestProcess message) {
        Long showId = message.requestBody.showId();
        Long userId = message.requestBody.userId();
        Long seatsBooked = message.requestBody.seatsBooked();

        ActorRef<ShowActor.Command> showActor = message.showMap().get(showId);
        if (!isUserExist()) {
            message.replyTo().tell(new BookingRegistry.CreateBookingResponse(null, StatusCodes.BAD_REQUEST,
                    "User not exists"));
            return Behaviors.stopped();
        }

        String paymentResponse = payment();
        if (!paymentResponse.equalsIgnoreCase("SUCCESS")) {
            message.replyTo().tell(new BookingRegistry.CreateBookingResponse(null, StatusCodes.BAD_REQUEST,
                    "Payment failed"));
            return Behaviors.stopped();
        }

        // create booking
        CompletionStage<ShowActor.Booking> completion = AskPattern.ask(showActor,
                ref -> new ShowActor.CreateShowBooking(ref, userId, seatsBooked), askTimeout, scheduler);
        completion.thenAccept(response -> {
            if (response.id() == -1) {
                message.replyTo().tell(new BookingRegistry.CreateBookingResponse(null, StatusCodes.BAD_REQUEST,
                        "Seats not Available"));
                // refund;
            } else {
                message.replyTo
                        .tell(new BookingRegistry.CreateBookingResponse(
                                new BookingRegistry.CreateBookingResponseBody(
                                        response.id(), response.showId(), response.userId(), response.seatsBooked()),
                                StatusCodes.OK, ""));
            }
        });
        return Behaviors.stopped();
    }

    private String payment() {
        return "SUCCESS";
    }

    private Boolean isUserExist() {
        return true;
    }
}
