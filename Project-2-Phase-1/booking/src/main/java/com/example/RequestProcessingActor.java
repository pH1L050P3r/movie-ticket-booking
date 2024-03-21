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

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(GetShowRequestProcess.class, this::onGetShow)
                .onMessage(GetTheatreRequestProcess.class, this::onGetTheatre)
                .onMessage(GetTheatreAllShowsRequestProcess.class, this::onGetTheatreAllShows).build();
    }

    private Behavior<Command> onGetShow(GetShowRequestProcess command) {
        ActorRef<ShowActor.Command> showActor = command.showMap().get(command.showId());
        if (showActor != null) {
            CompletionStage<ShowActor.Show> completion = AskPattern.ask(showActor,
                    ref -> new ShowActor.GetShow(ref), askTimeout, scheduler);
            completion.thenAccept(response -> {
                command.retplyTo().tell(new BookingRegistry.GetShowResponse(response));
            });
        } else {
            command.retplyTo().tell(new BookingRegistry.GetShowResponse(null));
        }
        return Behaviors.stopped();
    }

    private Behavior<Command> onGetTheatre(GetTheatreRequestProcess command) {
        ActorRef<TheatreActor.Command> theatreActor = command.theatreMap().get(command.theatreId());
        if (theatreActor != null) {
            CompletionStage<TheatreActor.Theatre> completion = AskPattern.ask(theatreActor,
                    ref -> new TheatreActor.GetTheatre(ref), askTimeout, scheduler);
            completion.thenAccept(response -> {
                command.replyTo().tell(new BookingRegistry.GetTheatreResponse(response));
            });
        } else {
            command.replyTo().tell(new BookingRegistry.GetTheatreResponse(null));
        }
        return Behaviors.stopped();
    }

    private Behavior<Command> onGetTheatreAllShows(GetTheatreAllShowsRequestProcess command) {
        ActorRef<TheatreActor.Command> theatreActor = command.theatreMap().get(command.theatreId());
        if (theatreActor != null) {
            CompletionStage<ShowActor.Shows> completion = AskPattern.ask(theatreActor,
                    ref -> new TheatreActor.GetThreatreShows(ref), askTimeout, scheduler);
            completion.thenAccept(response -> {
                command.replyTo().tell(new BookingRegistry.GetTheatreAllShowsResponse(response.shows()));
            });
        } else {
            List<ShowActor.Show> empltyList = new ArrayList<>();
            command.replyTo().tell(new BookingRegistry.GetTheatreAllShowsResponse(empltyList));
        }
        return Behaviors.stopped();
    }
}
