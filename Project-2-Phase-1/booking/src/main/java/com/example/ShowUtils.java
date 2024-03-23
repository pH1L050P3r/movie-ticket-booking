package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import com.example.ShowActor.Show;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class ShowUtils {

  private ShowUtils() {}

  public static Show getShowFromShowActor(
    ActorRef<ShowActor.Command> showActor,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    CompletionStage<ShowActor.Show> completion = AskPattern.ask(
      showActor,
      ShowActor.GetShow::new,
      askTimeout,
      scheduler
    );
    return completion.toCompletableFuture().join();
  }
}
