package com.example;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public class StartApp {

  static void startHttpServer(Route route, ActorSystem<?> system) {
    CompletionStage<ServerBinding> futureBinding = Http
      .get(system)
      .newServerAt("0.0.0.0", 8080)
      .bind(route);

    futureBinding.whenComplete((binding, exception) -> {
      if (binding != null) {
        InetSocketAddress address = binding.localAddress();
        system
          .log()
          .info(
            "Server online at http://{}:{}/",
            address.getHostString(),
            address.getPort()
          );
      } else {
        system
          .log()
          .error("Failed to bind HTTP endpoint, terminating system", exception);
        system.terminate();
      }
    });
  }

  public static void main(String[] args) throws Exception {
    Behavior<NotUsed> rootBehavior = Behaviors.setup(context -> {
      ActorRef<WalletRegistry.Command> walletRegistryActor = context.spawn(
        WalletRegistry.create(),
        "WalletRegistry"
      );

      WalletRoutes walletRoutes = new WalletRoutes(
        context.getSystem(),
        walletRegistryActor
      );
      startHttpServer(walletRoutes.urlRoute(), context.getSystem());

      return Behaviors.empty();
    });
    ActorSystem.create(rootBehavior, "HelloAkkaHttpServer");
  }
}
