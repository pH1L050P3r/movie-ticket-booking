package com.example.show;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.serialization.jackson.CborSerializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowActor extends AbstractBehavior<ShowActor.Command> {

  public Long id;
  public String title;
  public Long price;
  public Long seatsAvailable;
  public Long theatreId;

  // for generating unique id for each booking
  public Long bookingNextId = 1L;

  public static final Logger log = LoggerFactory.getLogger(ShowActor.class);
  public final Map<Long, List<Booking>> bookings;

  public static final EntityTypeKey<Command> TypeKey = EntityTypeKey.create(
    ShowActor.Command.class,
    "ShowActorEntity"
  );

  public interface Command extends CborSerializable {}

  public final record Initialize(
    Long id,
    String title,
    Long price,
    Long seatsAvailable,
    Long theatreId
  )
    implements Command {}

  public final record GetShow(ActorRef<Show> replyTo) implements Command {}

  public final record GetUserBookings(ActorRef<Bookings> replyTo, Long userId)
    implements Command {}

  public final record CreateShowBooking(
    ActorRef<Booking> replyTo,
    Long userId,
    Long seatsBooked
  )
    implements Command {}

  public final record DeleteUserShowBookings(
    ActorRef<DeleteBookingResponse> replyTo,
    Long userId
  )
    implements Command {}

  public final record DeleteAllBookings(ActorRef<DeleteBookingResponse> replyTo)
    implements Command {}

  public final record DeleteBookingResponse(Map<Long, Long> refundUserAmountMap)
    implements Command {}

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  public static final class Show implements Command {

    public Long id;
    public String title;
    public Long price;

    @JsonProperty("theatre_id")
    public Long theatreId;

    @JsonProperty("seats_available")
    public Long seatsAvailable;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  public static final class Shows implements Command {

    List<Show> shows;
  }

  public static final record Booking(
    Long id,
    @JsonProperty("show_id") Long showId,
    @JsonProperty("user_id") Long userId,
    @JsonProperty("seats_booked") Long seatsBooked
  )
    implements Command {}

  public static final record Bookings(List<Booking> bookings)
    implements Command {}

  public static Behavior<ShowActor.Command> create() {
    return Behaviors.setup(ShowActor::new);
  }

  private ShowActor(ActorContext<Command> context) {
    super(context);
    this.bookings = new HashMap<>();
  }

  private Long getBookingId() {
    return bookingNextId++;
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
      .onMessage(Initialize.class, this::onInitialize)
      .onMessage(GetShow.class, this::onGetShow)
      .onMessage(CreateShowBooking.class, this::onCreateShowBooking)
      .onMessage(GetUserBookings.class, this::onGetUserAllBookings)
      .onMessage(DeleteUserShowBookings.class, this::onDeleteUserAllBooking)
      .onMessage(DeleteAllBookings.class, this::onDeleteAllBookings)
      .build();
  }

  private Behavior<Command> onInitialize(Initialize command) {
    this.id = command.id();
    this.price = command.price();
    this.title = command.title();
    this.seatsAvailable = command.seatsAvailable();
    this.theatreId = command.theatreId();
    return this;
  }

  private Behavior<Command> onGetShow(GetShow command) {
    Show show = new Show(id, title, price, theatreId, seatsAvailable);
    command.replyTo().tell(show);
    return this;
  }

  private Behavior<Command> onCreateShowBooking(CreateShowBooking message) {
    if (bookings.get(message.userId()) == null) bookings.put(
      message.userId(),
      new LinkedList<>()
    );

    Booking booking;
    if (this.seatsAvailable < message.seatsBooked()) {
      booking = new Booking(-1L, -1L, -1L, -1L);
    } else {
      booking =
        new Booking(
          getBookingId(),
          this.id,
          message.userId(),
          message.seatsBooked()
        );
      this.seatsAvailable -= message.seatsBooked();
      bookings.get(message.userId()).add(booking);
    }
    message.replyTo().tell(booking);
    return this;
  }

  private Behavior<Command> onGetUserAllBookings(GetUserBookings message) {
    List<Booking> userBookings = bookings.get(message.userId());
    message.replyTo().tell(new Bookings(userBookings));
    return this;
  }

  private Behavior<Command> onDeleteUserAllBooking(
    DeleteUserShowBookings message
  ) {
    List<Booking> userBookings = bookings.get(message.userId());
    Map<Long, Long> userAmountRefundMapping = new HashMap<>();

    if (userBookings == null) {
      message.replyTo.tell(new DeleteBookingResponse(userAmountRefundMapping));
      return this;
    }

    Long totalAmount = 0L;
    Long totalSeatsToRestore = 0L;
    for (Booking booking : userBookings) {
      totalAmount += (booking.seatsBooked * price);
      totalSeatsToRestore += booking.seatsBooked();
    }
    userAmountRefundMapping.put(message.userId(), totalAmount);
    message.replyTo.tell(new DeleteBookingResponse(userAmountRefundMapping));
    bookings.remove(message.userId());
    seatsAvailable += totalSeatsToRestore;
    return this;
  }

  private Behavior<Command> onDeleteAllBookings(DeleteAllBookings message) {
    Map<Long, Long> userAmountRefundMapping = new HashMap<>();
    if (bookings.isEmpty()) {
      message.replyTo.tell(new DeleteBookingResponse(userAmountRefundMapping));
      return this;
    }

    Long totalSeatsToRestore = 0L;
    for (Entry<Long, List<Booking>> booking : bookings.entrySet()) {
      Long amountRefund = 0L;
      List<Booking> userBookings = bookings.get(booking.getKey());
      if (userBookings.isEmpty()) continue;

      for (Booking userBooking : booking.getValue()) {
        amountRefund += (userBooking.seatsBooked * price);
        totalSeatsToRestore += userBooking.seatsBooked();
      }
      userAmountRefundMapping.put(booking.getKey(), amountRefund);
    }

    seatsAvailable += totalSeatsToRestore;
    bookings.clear();
    message.replyTo.tell(new DeleteBookingResponse(userAmountRefundMapping));
    return this;
  }
}
