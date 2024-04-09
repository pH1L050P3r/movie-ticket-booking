package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import com.example.wallet.WalletActor;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalletRegistry extends AbstractBehavior<WalletRegistry.Command> {

  private Map<Long, ActorRef<WalletActor.Command>> walletMap = new HashMap<>();

  private static final Logger log = LoggerFactory.getLogger(
    WalletRegistry.class
  );

  sealed interface Command {}

  public static final record GetWalletRequest(
    ActorRef<GetWalletResponse> replyTo,
    Long userId
  )
    implements Command {}

  public static final record UpdateWalletRequest(
    ActorRef<UpdateWalletResponse> replyTo,
    Long userId,
    UpdateWalletRequestBody body
  )
    implements Command {}

  public static final record DeleteWalletByIdRequest(
    ActorRef<DeleteWalletResponse> replyTo,
    Long userId
  )
    implements Command {}

  public static final record DeleteAllWalletRequest(
    ActorRef<DeleteWalletResponse> replyTo
  )
    implements Command {}

  public static final record GetWalletResponse(
    StatusCode statusCode,
    WalletActor.Wallet body,
    String message
  )
    implements Command {}

  public static final record UpdateWalletResponse(
    StatusCode statusCode,
    WalletActor.Wallet body,
    String message
  )
    implements Command {}

  public static final record DeleteWalletResponse(
    StatusCode statusCode,
    String message
  )
    implements Command {}

  public static final record UpdateWalletRequestBody(
    Long amount,
    String action
  ) {}

  public static Behavior<WalletRegistry.Command> create() {
    return Behaviors.setup(WalletRegistry::new);
  }

  private WalletRegistry(ActorContext<WalletRegistry.Command> context) {
    super(context);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
      .onMessage(GetWalletRequest.class, this::onGetWallet)
      .onMessage(UpdateWalletRequest.class, this::onUpdateWallet)
      .onMessage(DeleteWalletByIdRequest.class, this::onDeleteWalletById)
      .onMessage(DeleteAllWalletRequest.class, this::onDeleteAllWallet)
      .build();
  }

  private Behavior<Command> onGetWallet(GetWalletRequest command) {
    ActorRef<WalletActor.Command> wallet = walletMap.get(command.userId());
    if (wallet == null) {
      command
        .replyTo()
        .tell(
          new GetWalletResponse(StatusCodes.NOT_FOUND, null, "Wallet not exist")
        );
    } else {
      wallet.tell(new WalletActor.GetWallet(command.replyTo()));
    }
    return this;
  }

  private Behavior<Command> onUpdateWallet(UpdateWalletRequest command) {
    ActorRef<WalletActor.Command> wallet = walletMap.get(command.userId());
    if (wallet == null) {
      wallet =
        getContext()
          .spawn(
            WalletActor.create(command.userId(), 0L),
            "wallet-" + command.userId().toString()
          );

      walletMap.put(command.userId(), wallet);
      wallet.tell(
        new WalletActor.UpdateWallet(command.replyTo(), command.body())
      );
    } else {
      wallet.tell(
        new WalletActor.UpdateWallet(command.replyTo(), command.body())
      );
    }
    return this;
  }

  private Behavior<Command> onDeleteWalletById(
    DeleteWalletByIdRequest command
  ) {
    ActorRef<WalletActor.Command> wallet = walletMap.get(command.userId);
    if (wallet == null) {
      command
        .replyTo()
        .tell(
          new DeleteWalletResponse(StatusCodes.NOT_FOUND, "wallet not exists")
        );
    } else {
      walletMap.remove(command.userId());
      wallet.tell(new WalletActor.DeleteWallet(command.replyTo()));
    }
    return this;
  }

  private Behavior<Command> onDeleteAllWallet(DeleteAllWalletRequest command) {
    for (ActorRef<WalletActor.Command> wallet : walletMap.values()) {
      wallet.tell(new WalletActor.DeleteWallet(command.replyTo()));
    }
    walletMap.clear();
    command
      .replyTo()
      .tell(new DeleteWalletResponse(StatusCodes.OK, "All wallet deleted"));
    return this;
  }
}
