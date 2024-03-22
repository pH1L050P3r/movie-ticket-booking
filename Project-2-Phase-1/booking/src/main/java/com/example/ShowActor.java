package com.example;

import akka.actor.typed.javadsl.AbstractBehavior;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private final static Map<Long, List<Booking>> bookings = new HashMap<Long, List<Booking>>();

    sealed interface Command {
    }

    public final static record GetShow(ActorRef<Show> replyTo) implements Command {
    }

    public final static record CreateShowBooking(ActorRef<Booking> replyTo, Long userId, Long seatsBooked)
            implements Command {
    }

    public final static record Show(Long id, String title, Long price, Long theatreId, Long seatsAvailable) {
    };

    public final static record Shows(List<Show> shows) {
    }

    public final static record Booking(Long id, Long showId, Long userId, Long seatsBooked) {
    }

    public final static record Bookings(List<Booking> bookings) {
    }

    public static Behavior<ShowActor.Command> create(Long id, String title, Long price, Long seatsAvailable,
            Long theatreId, ActorRef<TheatreActor.Command> theatreActor) {
        return Behaviors
                .setup(context -> new ShowActor(context, id, title, price, seatsAvailable, theatreId,
                        theatreActor));
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
        return newReceiveBuilder()
                .onMessage(GetShow.class, this::onGetShow)
                .onMessage(CreateShowBooking.class, this::onCreateShowBooking)
                .build();
    }

    private Behavior<Command> onGetShow(GetShow command) {
        Show show = new Show(id, title, price, theatreId, seatsAvailable);
        command.replyTo().tell(show);
        return this;
    }

    private Behavior<Command> onCreateShowBooking(CreateShowBooking message) {
        if (bookings.get(message.userId()) == null)
            bookings.put(message.userId(), new LinkedList<>());

        Booking booking;

        if (this.seatsAvailable < message.seatsBooked()) {
            booking = new Booking(-1L, -1L, -1L, -1L);
        } else {
            booking = new Booking(message.userId(), this.id, message.userId(), message.seatsBooked());
            bookings.get(message.userId()).add(booking);
        }
        message.replyTo().tell(booking);
        return this;
    }
}
