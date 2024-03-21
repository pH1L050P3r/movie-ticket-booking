package com.example;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class TheatreActor extends AbstractBehavior<TheatreActor.Command> {
    private Long id;
    private String name;
    private String location;
    private Map<Long, ActorRef<ShowActor.Command>> shows;
    private final static Logger log = LoggerFactory.getLogger(BookingRegistry.class);
    private final Duration askTimeout;
    private final Scheduler scheduler;

    sealed interface Command {
    }

    public final static record GetTheatre(ActorRef<Theatre> replyTo) implements Command {
    }

    public final static record GetThreatreShows(ActorRef<ShowActor.Shows> replyTo) implements Command {
    }

    public final static record Theatre(Long id, String name, String location) {
    }

    public final static record TheaterShows(List<ShowActor.Show> shows) {
    }

    public final static record UpdateShows(Long showId, ActorRef<ShowActor.Command> show) implements Command {
    }

    public static Behavior<TheatreActor.Command> create(Long id, String name, String location) {
        return Behaviors.setup(context -> new TheatreActor(context, id, name, location));
    }

    private TheatreActor(ActorContext<Command> context, Long id, String name, String location) {
        super(context);
        this.id = id;
        this.name = name;
        this.location = location;
        this.askTimeout = Duration.ofSeconds(5);
        this.scheduler = getContext().getSystem().scheduler();
        this.shows = new HashMap<>();
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(UpdateShows.class, this::onUpdateShows)
                .onMessage(GetTheatre.class, this::onGetTheatre)
                .onMessage(GetThreatreShows.class, this::onGetTheatreShows).build();
    }

    private Behavior<Command> onUpdateShows(UpdateShows command) {
        shows.put(command.showId(), command.show());
        return this;
    }

    private Behavior<Command> onGetTheatre(GetTheatre command) {
        command.replyTo().tell(new Theatre(id, name, location));
        return this;
    }

    private Behavior<Command> onGetTheatreShows(GetThreatreShows command) {
        List<ShowActor.Show> shows = new ArrayList<>();
        List<CompletionStage<ShowActor.Show>> completionStages = new ArrayList<>();

        for (ActorRef<ShowActor.Command> showActor : this.shows.values()) {
            CompletionStage<ShowActor.Show> completion = AskPattern.ask(showActor,
                    ref -> new ShowActor.GetShow(ref), askTimeout, scheduler);
            completionStages.add(completion);
        }

        for (CompletionStage<ShowActor.Show> completion : completionStages) {
            completion.thenAccept(response -> {
                shows.add(response);
            });
        }
        command.replyTo().tell(new ShowActor.Shows(shows));
        // getContext().spawn(RequestProcessingActor.create(), "actor")
        // .tell(new RequestProcessingActor.GetShowRequestProcess(command.retplyTo(),
        // command.id(),
        // Collections.unmodifiableMap(showsMap)));
        return this;
    }

}
