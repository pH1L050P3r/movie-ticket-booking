package com.example;

import akka.actor.typed.javadsl.AbstractBehavior;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowActor extends AbstractBehavior<ShowActor.Command> {
    private Long id;
    // private Theatre theatre;
    private String title;
    private Long price;
    private Long seatsAvailable;
    private ActorRef<TheatreActor.Command> theatreActor;
    private final static Logger log = LoggerFactory.getLogger(ShowActor.class);

    sealed interface Command {
    }

    public final static record GetShow(ActorRef<BookingRegistry.GetShowResponse> replyTo) implements Command {
    }

    public final static record Show(Long id, String title, Long price, Long seatsAvailable) {
    };

    public static Behavior<ShowActor.Command> create(Long id, String title, Long price, Long seatsAvailable,
            ActorRef<TheatreActor.Command> theatreActor) {
        return Behaviors.setup(context -> new ShowActor(context, id, title, price, seatsAvailable, theatreActor));
    }

    private ShowActor(ActorContext<Command> context, Long id, String title, Long price, Long seatsAvailable,
            ActorRef<TheatreActor.Command> theatreActor) {
        super(context);
        this.id = id;
        this.title = title;
        this.price = price;
        this.seatsAvailable = seatsAvailable;
        this.theatreActor = theatreActor;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(GetShow.class, this::onGetShow).build();
    }

    private Behavior<Command> onGetShow(GetShow command) {
        Show show = new Show(id, title, price, seatsAvailable);
        command.replyTo().tell(new BookingRegistry.GetShowResponse(show));
        return this;
    }
}
