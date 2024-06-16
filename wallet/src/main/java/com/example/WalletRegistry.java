package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.RecoveryCompleted;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.CommandHandlerBuilder;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventHandlerBuilder;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import akka.persistence.typed.javadsl.SignalHandler;
import com.example.wallet.WalletActor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalletRegistry
  extends EventSourcedBehavior<WalletRegistry.Command, WalletRegistry.Event, WalletRegistry.Registry> {

  private ActorContext<Command> context;
  private Map<Long, ActorRef<WalletActor.Command>> walletMap = new HashMap<>();

  private static final Logger log = LoggerFactory.getLogger(
    WalletRegistry.class
  );

  sealed interface Command extends CborSerializable {}

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
    WalletActor.WalletRes body,
    String message
  )
    implements Command {}

  public static final record UpdateWalletResponse(
    StatusCode statusCode,
    WalletActor.WalletRes body,
    String message
  )
    implements Command {}

  public static final record DeleteWalletResponse(
    StatusCode statusCode,
    String message
  )
    implements Command {}

  public static final record UpdateWalletRequestBody(Long amount, String action)
    implements Command {}

  // State
  // interface State extends CborSerializable {}

  public static class Registry implements CborSerializable {

    Set<Long> actorKeys;

    Registry() {
      actorKeys = new HashSet<>();
    }

    Registry(Set<Long> actorKeys) {
      this.actorKeys = actorKeys;
    }

    boolean isContainKey(Long key) {
      return actorKeys.contains(key);
    }

    Registry addActorKey(Long key) {
      actorKeys.add(key);
      // return this;
      return new Registry(actorKeys);
    }

    Registry removeActorKey(Long key) {
      actorKeys.remove(key);
      // return this;
      return new Registry(actorKeys);
    }

    Registry clear() {
      return new Registry();
    }
  }

  // Events
  interface Event extends CborSerializable {}

  public static record AddKeyEvent(Long key) implements Event {}

  public static record DeleteKeyEvent(Long key) implements Event {}

  public enum CleanEvent implements Event {
    INSTANCE,
  }

  public static Behavior<WalletRegistry.Command> create(
    PersistenceId persistenceId
  ) {
    return Behaviors.setup(context -> new WalletRegistry(context, persistenceId)
    );
  }

  public Registry emptyState() {
    return new Registry();
  }

  private WalletRegistry(
    ActorContext<WalletRegistry.Command> context,
    PersistenceId persistenceId
  ) {
    super(persistenceId);
    this.context = context;
  }

  @Override
  public CommandHandler<Command, Event, Registry> commandHandler() {
    CommandHandlerBuilder<Command, Event, Registry> builder = newCommandHandlerBuilder();

    builder
      .forStateType(Registry.class)
      .onCommand(GetWalletRequest.class, this::onGetWallet)
      .onCommand(UpdateWalletRequest.class, this::onUpdateWallet)
      .onCommand(DeleteWalletByIdRequest.class, this::onDeleteWalletById)
      .onCommand(DeleteAllWalletRequest.class, this::onDeleteAllWallet);

    return builder.build();
  }

  private Effect<Event, Registry> onGetWallet(
    Registry registry,
    GetWalletRequest command
  ) {
    ActorRef<WalletActor.Command> wallet = walletMap.get(command.userId());
    if (wallet == null) {
      command
        .replyTo()
        .tell(
          new WalletRegistry.GetWalletResponse(StatusCodes.NOT_FOUND, null, "")
        );
    } else {
      wallet.tell(new WalletActor.GetWallet(command.replyTo()));
    }
    return Effect().noReply();
  }

  private Effect<Event, Registry> onUpdateWallet(
    Registry registry,
    UpdateWalletRequest command
  ) {
    ActorRef<WalletActor.Command> wallet = walletMap.get(command.userId());
    if (wallet == null) {
      final ActorRef<WalletActor.Command> newWallet = context.spawn(
        WalletActor.create(
          PersistenceId.ofUniqueId("wallet-" + command.userId().toString()),
          command.userId()
        ),
        "wallet-" + command.userId().toString()
      );
      walletMap.put(command.userId(), newWallet);
      return Effect()
        .persist(new AddKeyEvent(command.userId()))
        .thenRun(() ->
          newWallet.tell(
            new WalletActor.UpdateWallet(command.replyTo(), command.body())
          )
        )
        .thenNoReply();
    }
    wallet.tell(
      new WalletActor.UpdateWallet(command.replyTo(), command.body())
    );
    return Effect().noReply();
  }

  private Effect<Event, Registry> onDeleteWalletById(
    Registry registry,
    DeleteWalletByIdRequest command
  ) {
    ActorRef<WalletActor.Command> wallet = walletMap.get(command.userId);
    if (wallet == null) {
      command
        .replyTo()
        .tell(
          new WalletRegistry.DeleteWalletResponse(
            StatusCodes.NOT_FOUND,
            "Wallet does not exist"
          )
        );
    } else {
      walletMap.remove(command.userId());
      wallet.tell(new WalletActor.DeleteWallet(command.replyTo(), true));
      return Effect()
        .persist(new DeleteKeyEvent(command.userId()))
        .thenNoReply();
    }
    return Effect().noReply();
  }

  private Effect<Event, Registry> onDeleteAllWallet(
    Registry registry,
    DeleteAllWalletRequest command
  ) {
    for (ActorRef<WalletActor.Command> wallet : walletMap.values()) {
      wallet.tell(new WalletActor.DeleteWallet(command.replyTo(), false));
    }
    walletMap.clear();
    return Effect()
      .persist(CleanEvent.INSTANCE)
      .thenRun(() ->
        command
          .replyTo()
          .tell(new DeleteWalletResponse(StatusCodes.OK, "All wallet deleted"))
      )
      .thenNoReply();
  }

  @Override
  public EventHandler<Registry, Event> eventHandler() {
    EventHandlerBuilder<Registry, Event> builder = newEventHandlerBuilder();

    builder
      .forStateType(Registry.class)
      .onEvent(
        AddKeyEvent.class,
        (registry, addKey) -> registry.addActorKey(addKey.key())
      )
      .onEvent(
        DeleteKeyEvent.class,
        (registry, deleteKey) -> registry.removeActorKey(deleteKey.key())
      )
      .onEvent(CleanEvent.class, (registry, clean) -> registry.clear());

    return builder.build();
  }

  @Override
  public SignalHandler<Registry> signalHandler() {
    return newSignalHandlerBuilder()
      .onSignal(
        RecoveryCompleted.instance(),
        state -> {
          // restore all actor's
          log.info("Keys : " + state.actorKeys.toString());
          for (Long key : state.actorKeys) {
            log.debug("Restored Actor with userId : " + key.toString());
            walletMap.put(
              key,
              context.spawn(
                WalletActor.create(
                  PersistenceId.ofUniqueId("wallet-" + key.toString()),
                  key
                ),
                "wallet-" + key.toString()
              )
            );
          }
        }
      )
      .build();
  }
}
