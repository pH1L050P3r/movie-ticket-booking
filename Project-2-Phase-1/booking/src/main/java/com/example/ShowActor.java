package com.example;

import akka.actor.typed.javadsl.AbstractBehavior;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowActor extends AbstractBehavior<ShowActor.Command> {
    private Long id;
    // private Theatre theatre;
    private String title;
    private Long price;
    private Long seatsAvailable;
    private Long theatreId;
    private ActorRef<TheatreActor.Command> theatreActor;
    private final static Logger log = LoggerFactory.getLogger(ShowActor.class);

    sealed interface Command {
    }

    public final static record GetShow(ActorRef<Show> replyTo) implements Command {
    }

    public final static record Show(Long id, String title, Long price, Long theatreId, Long seatsAvailable) {
    };

    public final static record Shows(List<Show> shows) {
    }

    public static Behavior<ShowActor.Command> create(Long id, String title, Long price, Long seatsAvailable,
            Long theatreId, ActorRef<TheatreActor.Command> theatreActor) {
        return Behaviors
                .setup(context -> new ShowActor(context, id, title, price, seatsAvailable, theatreId, theatreActor));
    }

    private ShowActor(ActorContext<Command> context, Long id, String title, Long price, Long seatsAvailable,
            Long theatreId, ActorRef<TheatreActor.Command> theatreActor) {
        super(context);
        this.id = id;
        this.title = title;
        this.price = price;
        this.seatsAvailable = seatsAvailable;
        this.theatreActor = theatreActor;
        this.theatreId = theatreId;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(GetShow.class, this::onGetShow).build();
    }

    private Behavior<Command> onGetShow(GetShow command) {
        Show show = new Show(id, title, price, theatreId, seatsAvailable);
        command.replyTo().tell(show);
        return this;
    }
}
