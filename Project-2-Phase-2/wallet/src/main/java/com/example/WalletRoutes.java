package com.example;

import static akka.http.javadsl.server.Directives.*;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class WalletRoutes {

  private final ActorRef<WalletRegistry.Command> walletRegistryActor;
  private final Duration askTimeout;
  private final Scheduler scheduler;

  // private static final Logger log = LoggerFactory.getLogger(WalletRoutes.class);

  public WalletRoutes(
    ActorSystem<?> system,
    ActorRef<WalletRegistry.Command> walletRegistryActor
  ) {
    this.walletRegistryActor = walletRegistryActor;
    this.scheduler = system.scheduler();
    askTimeout =
      system.settings().config().getDuration("my-app.routes.ask-timeout");
  }

  private CompletionStage<WalletRegistry.GetWalletResponse> getWallet(Long id) {
    return AskPattern.ask(
      walletRegistryActor,
      ref -> new WalletRegistry.GetWalletRequest(ref, id),
      askTimeout,
      scheduler
    );
  }

  private CompletionStage<WalletRegistry.UpdateWalletResponse> updateWallet(
    Long id,
    WalletRegistry.UpdateWalletRequestBody body
  ) {
    return AskPattern.ask(
      walletRegistryActor,
      ref -> new WalletRegistry.UpdateWalletRequest(ref, id, body),
      askTimeout,
      scheduler
    );
  }

  private CompletionStage<WalletRegistry.DeleteWalletResponse> deleteWalletById(
    Long id
  ) {
    return AskPattern.ask(
      walletRegistryActor,
      ref -> new WalletRegistry.DeleteWalletByIdRequest(ref, id),
      askTimeout,
      scheduler
    );
  }

  private CompletionStage<WalletRegistry.DeleteWalletResponse> deleteAllWallet() {
    return AskPattern.ask(
      walletRegistryActor,
      WalletRegistry.DeleteAllWalletRequest::new,
      askTimeout,
      scheduler
    );
  }

  public Route walletRoute() {
    return pathPrefix(
      "wallets",
      () ->
        concat(
          pathEnd(() ->
            delete(() ->
              onSuccess(
                deleteAllWallet(),
                response -> complete(response.statusCode(), response.message())
              )
            )
          ),
          path(
            PathMatchers.longSegment(),
            (Long userId) ->
              concat(
                get(() ->
                  onSuccess(
                    getWallet(userId),
                    response ->
                      complete(
                        response.statusCode(),
                        response.body(),
                        Jackson.marshaller()
                      )
                  )
                ),
                put(() ->
                  entity(
                    Jackson.unmarshaller(
                      WalletRegistry.UpdateWalletRequestBody.class
                    ),
                    bookingBody ->
                      onSuccess(
                        updateWallet(userId, bookingBody),
                        performed ->
                          complete(
                            performed.statusCode(),
                            performed.body(),
                            Jackson.marshaller()
                          )
                      )
                  )
                ),
                delete(() ->
                  onSuccess(
                    deleteWalletById(userId),
                    response ->
                      complete(
                        response.statusCode(),
                        response.message(),
                        Jackson.marshaller()
                      )
                  )
                )
              )
          )
        )
    );
  }

  public Route urlRoute() {
    return walletRoute();
  }
}
