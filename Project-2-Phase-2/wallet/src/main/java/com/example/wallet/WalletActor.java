package com.example.wallet;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.example.CborSerializable;
import com.example.WalletRegistry;
import com.example.services.UserService;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;

public class WalletActor
  extends EventSourcedBehavior<WalletActor.Command, WalletActor.WalletEvent, WalletActor.WalletState> {

  ActorContext<Command> context;

  private Long userId;
  private Long balance;
  private boolean isActive = false;

  private final Duration askTimeout;
  private final Scheduler scheduler;
  private final Http http;

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
    ActorRef<WalletRegistry.DeleteWalletResponse> replyTo
  )
    implements Command {}

  public static final record Wallet(
    @JsonProperty("user_id") Long userId,
    @JsonProperty("balance") Long balance
  ) {}

  // Event
  public interface WalletEvent extends CborSerializable {}

  // State
  static final class WalletState implements CborSerializable {

    public Long userId;
    public Long balance;
    public boolean isActive = false;
  }

  public static Behavior<WalletActor.Command> create(Long id, Long balance) {
    return Behaviors.setup(context -> new WalletActor(context, id, balance));
  }

  private WalletActor(
    ActorContext<Command> context,
    Long userId,
    Long balance
  ) {
    super(context);
    this.userId = userId;
    this.balance = balance;
    this.askTimeout =
      context
        .getSystem()
        .settings()
        .config()
        .getDuration("my-app.routes.ask-timeout");
    this.scheduler = context.getSystem().scheduler();
    this.http = Http.get(context.getSystem());
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
      .onMessage(GetWallet.class, this::onGetWallet)
      .onMessage(UpdateWallet.class, this::onUpdateWallet)
      .onMessage(DeleteWallet.class, this::onDeleteWallet)
      .build();
  }

  private Behavior<Command> onGetWallet(GetWallet command) {
    if (isActive) {
      command
        .replyTo()
        .tell(
          new WalletRegistry.GetWalletResponse(
            StatusCodes.OK,
            new Wallet(userId, balance),
            ""
          )
        );
    } else {
      command
        .replyTo()
        .tell(
          new WalletRegistry.GetWalletResponse(StatusCodes.NOT_FOUND, null, "")
        );
    }
    return this;
  }

  private Behavior<Command> onDeleteWallet(DeleteWallet command) {
    this.balance = 0L;
    this.isActive = false;

    command
      .replyTo()
      .tell(new WalletRegistry.DeleteWalletResponse(StatusCodes.OK, ""));

    return Behaviors.stopped();
  }

  private Behavior<Command> onUpdateWallet(UpdateWallet command) {
    if (!isActive && !UserService.isUserExist(userId, http)) {
      command
        .replyTo()
        .tell(
          new WalletRegistry.UpdateWalletResponse(
            StatusCodes.BAD_REQUEST,
            null,
            "User does not exit"
          )
        );
      return this;
    }
    isActive = true;
    if (
      "debit".equals(command.body().action()) &&
      balance >= command.body().amount()
    ) {
      this.balance -= command.body().amount();
      command
        .replyTo()
        .tell(
          new WalletRegistry.UpdateWalletResponse(
            StatusCodes.OK,
            new Wallet(userId, balance),
            ""
          )
        );
    } else if (
      "credit".equals(command.body().action()) && command.body().amount() >= 0
    ) {
      this.balance += command.body().amount();
      command
        .replyTo()
        .tell(
          new WalletRegistry.UpdateWalletResponse(
            StatusCodes.OK,
            new Wallet(userId, balance),
            ""
          )
        );
    } else {
      command
        .replyTo()
        .tell(
          new WalletRegistry.UpdateWalletResponse(
            StatusCodes.BAD_REQUEST,
            null,
            "action or amount is not correct"
          )
        );
    }
    return this;
  }
}
