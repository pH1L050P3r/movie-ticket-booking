package com.example.requestprocessor;

import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.example.show.ShowActor;
import com.example.show.ShowActor.DeleteBookingResponse;
import com.example.show.ShowActor.Show;
import com.example.theatre.TheatreActor;
import com.example.theatre.TheatreActor.Theatre;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class RequestProcessingUtils {

  private RequestProcessingUtils() {}

  public static Show getShowFromShowActor(
    EntityRef<ShowActor.Command> showActor,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    CompletionStage<ShowActor.Show> completion = AskPattern.ask(
      showActor,
      ShowActor.GetShow::new,
      askTimeout,
      scheduler
    );
    try {
      return completion.toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Theatre getTheatreFromTheatreActor(
    EntityRef<TheatreActor.Command> theatreActor,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    CompletionStage<TheatreActor.Theatre> completion = AskPattern.ask(
      theatreActor,
      TheatreActor.GetTheatre::new,
      askTimeout,
      scheduler
    );
    try {
      Theatre theatre = completion.toCompletableFuture().get();
      return theatre;
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static List<Theatre> getTheatreListFromTheatreActorList(
    Collection<EntityRef<TheatreActor.Command>> theatresActors,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    List<TheatreActor.Theatre> theatres = new ArrayList<>();
    List<CompletionStage<TheatreActor.Theatre>> completionStages = new ArrayList<>();

    for (EntityRef<TheatreActor.Command> theatreActor : theatresActors) {
      CompletionStage<TheatreActor.Theatre> completion = AskPattern.ask(
        theatreActor,
        TheatreActor.GetTheatre::new,
        askTimeout,
        scheduler
      );
      completionStages.add(completion);
    }

    try {
      for (CompletionStage<TheatreActor.Theatre> completion : completionStages) {
        Theatre theatre = completion.toCompletableFuture().get();
        theatres.add(theatre);
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return theatres;
  }

  public static List<Show> getTheatreAllShows(
    EntityRef<TheatreActor.Command> theatreActor,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    CompletionStage<ShowActor.Shows> completion = AskPattern.ask(
      theatreActor,
      TheatreActor.GetTheatreShows::new,
      askTimeout,
      scheduler
    );
    try {
      return completion.toCompletableFuture().get().getShows();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return Collections.emptyList();
  }

  public static DeleteBookingResponse deleteAllBookings(
    Collection<EntityRef<ShowActor.Command>> showActors,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    List<CompletionStage<ShowActor.DeleteBookingResponse>> completions = new ArrayList<>();
    Map<Long, Long> userRefundMapping = new HashMap<>();

    for (EntityRef<ShowActor.Command> showActor : showActors) {
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
    Collection<EntityRef<ShowActor.Command>> showActors,
    Long userId,
    Duration askTimeout,
    Scheduler scheduler
  ) {
    List<CompletionStage<ShowActor.DeleteBookingResponse>> completions = new ArrayList<>();
    Map<Long, Long> userRefundMapping = new HashMap<>();

    for (EntityRef<ShowActor.Command> showActor : showActors) {
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
