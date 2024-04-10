package com.example.wallet;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.CommandHandlerBuilder;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventHandlerBuilder;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.example.CborSerializable;
import com.example.WalletRegistry;
import com.example.WalletRegistry.GetWalletResponse;
import com.example.services.UserService;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;

public class WalletActor
  extends EventSourcedBehavior<WalletActor.Command, WalletActor.Event, WalletActor.Wallet> {

  ActorContext<Command> context;

  private final Duration askTimeout;
  private final Scheduler scheduler;
  private final Http http;

  private final Long walletId;

  // Commands
  public interface Command extends CborSerializable {}

  public static final record GetWallet(
    ActorRef<WalletRegistry.GetWalletResponse> replyTo
  )
    implements Command {}

  public static final record UpdateWallet(
    ActorRef<WalletRegistry.UpdateWalletResponse> replyTo,
    WalletRegistry.UpdateWalletRequestBody body
  )
    implements Command {}

  public static final record DeleteWallet(
    ActorRef<WalletRegistry.DeleteWalletResponse> replyTo,
    boolean isReplyRequired
  )
    implements Command {}

  // POJO class
  public static final record WalletRes(
    @JsonProperty("user_id") Long walletId,
    @JsonProperty("balance") Long balance
  ) {}

  // State
  public interface Wallet extends CborSerializable {}

  public static final class EmptyWallet implements Wallet {

    ActiveWallet startWallet(Long balance) {
      return new ActiveWallet(balance);
    }
  }

  public static final class ActiveWallet implements Wallet {

    Long balance;

    ActiveWallet(Long balance) {
      this.balance = balance;
    }

    ActiveWallet credit(Long amount) {
      return new ActiveWallet(balance + amount);
    }

    ActiveWallet debit(Long amount) {
      if (!canDebit(amount)) {
        throw new IllegalStateException("Insufficient balance.");
      }
      return new ActiveWallet(balance - amount);
    }

    boolean canDebit(Long amount) {
      return (balance - amount) >= 0;
    }

    ClosedWallet delete() {
      return new ClosedWallet();
    }
  }

  public static final class ClosedWallet implements Wallet {}

  public static Behavior<Command> create(
    PersistenceId persistenceId,
    Long userId
  ) {
    return Behaviors.setup(context ->
      new WalletActor(context, persistenceId, userId)
    );
  }

  public WalletActor(
    ActorContext<Command> context,
    PersistenceId persistenceId,
    Long userId
  ) {
    super(persistenceId);
    this.context = context;
    this.askTimeout =
      context
        .getSystem()
        .settings()
        .config()
        .getDuration("my-app.routes.ask-timeout");
    this.scheduler = context.getSystem().scheduler();
    this.http = Http.get(context.getSystem());
    this.walletId = userId;
  }

  // Events
  interface Event extends CborSerializable {}

  public enum GetEvent implements Event {
    INSTANCE,
  }

  public static record CreditEvent(Long balance) implements Event {}

  public static record DebitEvent(Long balance) implements Event {}

  public enum DeleteEvent implements Event {
    INSTANCE,
  }

  @Override
  public Wallet emptyState() {
    return new EmptyWallet();
  }

  @Override
  public CommandHandler<Command, Event, Wallet> commandHandler() {
    CommandHandlerBuilder<Command, Event, Wallet> builder = newCommandHandlerBuilder();

    builder
      .forStateType(EmptyWallet.class)
      .onCommand(GetWallet.class, this::getWallet)
      .onCommand(UpdateWallet.class, this::updateWallet);

    builder
      .forStateType(ActiveWallet.class)
      .onCommand(GetWallet.class, this::getWallet)
      .onCommand(UpdateWallet.class, this::updateWallet)
      .onCommand(DeleteWallet.class, this::deleteWallet);

    return builder.build();
  }

  private Effect<Event, Wallet> getWallet(
    ActiveWallet wallet,
    GetWallet command
  ) {
    return Effect()
      .reply(
        command.replyTo,
        new WalletRegistry.GetWalletResponse(
          StatusCodes.OK,
          new WalletRes(walletId, wallet.balance),
          ""
        )
      );
  }

  private Effect<Event, Wallet> getWallet(
    EmptyWallet wallet,
    GetWallet command
  ) {
    return Effect()
      .reply(
        command.replyTo,
        new WalletRegistry.GetWalletResponse(StatusCodes.NOT_FOUND, null, "")
      );
  }

  private Effect<Event, Wallet> updateWallet(
    EmptyWallet wallet,
    UpdateWallet command
  ) {
    if (
      "debit".equals(command.body().action()) || command.body().amount() < 0
    ) {
      return Effect()
        .reply(
          command.replyTo(),
          new WalletRegistry.UpdateWalletResponse(
            StatusCodes.BAD_REQUEST,
            null,
            "Insufficient Balance"
          )
        );
    }

    if ("credit".equals(command.body().action())) {
      return Effect()
        .persist(new CreditEvent(command.body().amount()))
        .thenReply(
          command.replyTo,
          newWallet ->
            new WalletRegistry.UpdateWalletResponse(
              StatusCodes.OK,
              new WalletRes(walletId, ((ActiveWallet) newWallet).balance),
              ""
            )
        );
    } else {
      return Effect()
        .persist(new DebitEvent(command.body().amount()))
        .thenReply(
          command.replyTo,
          newWallet ->
            new WalletRegistry.UpdateWalletResponse(
              StatusCodes.OK,
              new WalletRes(walletId, ((ActiveWallet) newWallet).balance),
              ""
            )
        );
    }
  }

  private Effect<Event, Wallet> updateWallet(
    ActiveWallet wallet,
    UpdateWallet command
  ) {
    if (!UserService.isUserExist(walletId, http)) {
      return Effect()
        .reply(
          command.replyTo(),
          new WalletRegistry.UpdateWalletResponse(
            StatusCodes.BAD_REQUEST,
            null,
            "Insufficient Balance"
          )
        );
    }
    if ("credit".equals(command.body().action())) {
      return Effect()
        .persist(new CreditEvent(command.body().amount()))
        .thenReply(
          command.replyTo,
          newWallet ->
            new WalletRegistry.UpdateWalletResponse(
              StatusCodes.OK,
              new WalletRes(walletId, ((ActiveWallet) newWallet).balance),
              ""
            )
        );
    } else if (wallet.balance >= command.body().amount()) {
      return Effect()
        .persist(new DebitEvent(command.body().amount()))
        .thenReply(
          command.replyTo,
          newWallet ->
            new WalletRegistry.UpdateWalletResponse(
              StatusCodes.OK,
              new WalletRes(walletId, ((ActiveWallet) newWallet).balance),
              ""
            )
        );
    } else {
      return Effect()
        .reply(
          command.replyTo(),
          new WalletRegistry.UpdateWalletResponse(
            StatusCodes.BAD_REQUEST,
            null,
            "Insufficient Balance"
          )
        );
    }
  }

  private Effect<Event, Wallet> deleteWallet(
    ActiveWallet wallet,
    DeleteWallet command
  ) {
    if (command.isReplyRequired()) {
      return Effect()
        .persist(DeleteEvent.INSTANCE)
        .thenReply(
          command.replyTo(),
          newWallet ->
            new WalletRegistry.DeleteWalletResponse(StatusCodes.OK, "")
        );
    } else {
      return Effect().persist(DeleteEvent.INSTANCE).thenNoReply();
    }
  }

  @Override
  public EventHandler<Wallet, Event> eventHandler() {
    EventHandlerBuilder<Wallet, Event> builder = newEventHandlerBuilder();

    builder
      .forStateType(EmptyWallet.class)
      .onEvent(
        CreditEvent.class,
        (wallet, credit) -> wallet.startWallet(credit.balance())
      );

    builder
      .forStateType(ActiveWallet.class)
      .onEvent(
        CreditEvent.class,
        (wallet, credit) -> wallet.credit(credit.balance())
      )
      .onEvent(
        DebitEvent.class,
        (wallet, debit) -> wallet.debit(debit.balance())
      )
      .onEvent(DeleteEvent.class, (wallet, delete) -> wallet.delete());

    return builder.build();
  }
}
