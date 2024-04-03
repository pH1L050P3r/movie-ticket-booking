package com.example.requestprocessor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import com.example.show.ShowActor;
import com.example.show.ShowActor.DeleteBookingResponse;
import com.example.show.ShowActor.Show;
import com.example.theatre.TheatreActor;
import com.example.theatre.TheatreActor.Theatre;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;

public class RequestProcessingUtils {

  private RequestProcessingUtils() {}

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

  public static Theatre getTheatreFromTheatreActor(
    ActorRef<TheatreActor.Command> theatreActor,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    CompletionStage<TheatreActor.Theatre> completion = AskPattern.ask(
      theatreActor,
      TheatreActor.GetTheatre::new,
      askTimeout,
      scheduler
    );
    return completion.toCompletableFuture().join();
  }

  public static List<Theatre> getTheatreListFromTheatreActorList(
    Collection<ActorRef<TheatreActor.Command>> theatresActors,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    List<TheatreActor.Theatre> theatres = new ArrayList<>();
    List<CompletionStage<TheatreActor.Theatre>> completionStages = new ArrayList<>();

    for (ActorRef<TheatreActor.Command> theatreActor : theatresActors) {
      CompletionStage<TheatreActor.Theatre> completion = AskPattern.ask(
        theatreActor,
        TheatreActor.GetTheatre::new,
        askTimeout,
        scheduler
      );
      completionStages.add(completion);
    }

    for (CompletionStage<TheatreActor.Theatre> completion : completionStages) {
      completion.thenAccept(theatres::add);
    }
    return theatres;
  }

  public static List<Show> getTheatreAllShows(
    ActorRef<TheatreActor.Command> theatreActor,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    CompletionStage<ShowActor.Shows> completion = AskPattern.ask(
      theatreActor,
      TheatreActor.GetTheatreShows::new,
      askTimeout,
      scheduler
    );
    return completion.toCompletableFuture().join().shows();
  }

  public static DeleteBookingResponse deleteAllBookings(
    Collection<ActorRef<ShowActor.Command>> showActors,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    List<CompletionStage<ShowActor.DeleteBookingResponse>> completions = new ArrayList<>();
    Map<Long, Long> userRefundMapping = new HashMap<>();

    for (ActorRef<ShowActor.Command> showActor : showActors) {
      completions.add(
        AskPattern.ask(
          showActor,
          ShowActor.DeleteAllBookings::new,
          askTimeout,
          scheduler
        )
      );
    }

    for (CompletionStage<ShowActor.DeleteBookingResponse> completion : completions) {
      DeleteBookingResponse response = completion.toCompletableFuture().join();
      for (Entry<Long, Long> user : response.refundUserAmountMap().entrySet()) {
        Long userId = user.getKey();
        Long amount = user.getValue();
        if (userRefundMapping.containsKey(userId)) {
          userRefundMapping.put(userId, userRefundMapping.get(userId) + amount);
        } else {
          userRefundMapping.put(userId, amount);
        }
      }
    }
    return new DeleteBookingResponse(userRefundMapping);
  }

  public static DeleteBookingResponse deleteAllUserBookings(
    Collection<ActorRef<ShowActor.Command>> showActors,
    Long userId,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    List<CompletionStage<ShowActor.DeleteBookingResponse>> completions = new ArrayList<>();
    Map<Long, Long> userRefundMapping = new HashMap<>();

    for (ActorRef<ShowActor.Command> showActor : showActors) {
      completions.add(
        AskPattern.ask(
          showActor,
          ref -> new ShowActor.DeleteUserShowBookings(ref, userId),
          askTimeout,
          scheduler
        )
      );
    }

    Long totalAmount = 0L;
    for (CompletionStage<ShowActor.DeleteBookingResponse> completion : completions) {
      DeleteBookingResponse response = completion.toCompletableFuture().join();
      for (Entry<Long, Long> user : response.refundUserAmountMap().entrySet()) {
        totalAmount += user.getValue();
      }
    }
    userRefundMapping.put(userId, totalAmount);
    return new DeleteBookingResponse(userRefundMapping);
  }
}
