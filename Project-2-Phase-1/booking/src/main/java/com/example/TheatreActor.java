package com.example;

import java.util.ArrayList;
import java.util.List;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class TheatreActor extends AbstractBehavior<TheatreActor.Command> {
    private Long id;
    private String name;
    private String location;
    private List<ActorRef<ShowActor.Command>> shows = new ArrayList<>();

    sealed interface Command {
    }

    public final static record GetTheatre(ActorRef<BookingRegistry.GetShow> replyTo) implements Command {
    }

    public final static record UpdateShows(ActorRef<ShowActor.Command> show) implements Command {
    }

    public static Behavior<TheatreActor.Command> create(Long id, String name, String location) {
        return Behaviors.setup(context -> new TheatreActor(context, id, name, location));
    }

    private TheatreActor(ActorContext<Command> context, Long id, String name, String location) {
        super(context);
        this.id = id;
        this.name = name;
        this.location = location;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(UpdateShows.class, this::onUpdateShows).build();
    }

    public Behavior<Command> onUpdateShows(UpdateShows command) {
        shows.add(command.show());
        return this;
    }
}
