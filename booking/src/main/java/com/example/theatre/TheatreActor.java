package com.example.theatre;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.serialization.jackson.CborSerializable;
import com.example.show.ShowActor;
import com.example.show.ShowActor.Show;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheatreActor extends AbstractBehavior<TheatreActor.Command> {

  public Long id;
  public String name;
  public String location;
  public List<Long> showsId;
  public static final Logger log = LoggerFactory.getLogger(TheatreActor.class);
  public final Duration askTimeout;
  public final Scheduler scheduler;
  public final ClusterSharding sharding;

  public static final EntityTypeKey<Command> TypeKey = EntityTypeKey.create(
    TheatreActor.Command.class,
    "TheatreActorEntity"
  );

  public interface Command extends CborSerializable {}

  public final record Initialize(Long id, String name, String location)
    implements Command {}

  public final record GetTheatre(ActorRef<Theatre> replyTo)
    implements Command {}

  public final record GetTheatreShows(ActorRef<ShowActor.Shows> replyTo)
    implements Command {}

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  public static final class Theatre implements Command {

    public Long id;
    public String name;
    public String location;
  }

  public final record TheaterShows(List<ShowActor.Show> shows)
    implements Command {}

  public final record UpdateShows(
    Long showId,
    EntityRef<ShowActor.Command> show
  )
    implements Command {}

  public static Behavior<TheatreActor.Command> create() {
    return Behaviors.setup(TheatreActor::new);
  }

  private TheatreActor(ActorContext<Command> context) {
    super(context);
    sharding = ClusterSharding.get(context.getSystem());
    this.askTimeout =
      context
        .getSystem()
        .settings()
        .config()
        .getDuration("my-app.routes.ask-timeout");
    this.scheduler = getContext().getSystem().scheduler();
    this.showsId = new ArrayList<>();
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
      .onMessage(Initialize.class, this::onTheatreInitialization)
      .onMessage(UpdateShows.class, this::onUpdateShows)
      .onMessage(GetTheatre.class, this::onGetTheatre)
      .onMessage(GetTheatreShows.class, this::onGetTheatreShows)
      .build();
  }

  private Behavior<Command> onTheatreInitialization(Initialize command) {
    this.id = command.id();
    this.name = command.name();
    this.location = command.location();
    return this;
  }

  private Behavior<Command> onUpdateShows(UpdateShows command) {
    showsId.add(command.showId());
    return this;
  }

  private Behavior<Command> onGetTheatre(GetTheatre command) {
    command.replyTo().tell(new Theatre(id, name, location));
    return this;
  }

  private Behavior<Command> onGetTheatreShows(GetTheatreShows command) {
    List<ShowActor.Show> showList = new ArrayList<>();
    List<CompletionStage<ShowActor.Show>> completionStages = new ArrayList<>();

    for (Long showId : this.showsId) {
      EntityRef<ShowActor.Command> showActor = sharding.entityRefFor(
        ShowActor.TypeKey,
        "Show-" + Long.toString(showId)
      );
      CompletionStage<ShowActor.Show> completion = AskPattern.ask(
        showActor,
        ShowActor.GetShow::new,
        askTimeout,
        scheduler
      );
      completionStages.add(completion);
    }

    try {
      for (CompletionStage<ShowActor.Show> completion : completionStages) {
        Show show = completion.toCompletableFuture().get();
        showList.add(show);
      }
    } catch (Exception e) {}
    log.info(showList.toString());
    command.replyTo().tell(new ShowActor.Shows(showList));
    return this;
  }
}
