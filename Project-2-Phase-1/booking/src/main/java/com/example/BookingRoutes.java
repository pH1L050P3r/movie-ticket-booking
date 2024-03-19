package com.example;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.concat;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.onSuccess;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.rejectEmptyResponse;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;

public class BookingRoutes {
    private final ActorRef<BookingRegistry.Command> bookingRegistryActor;
    private final Duration askTimeout;
    private final Scheduler scheduler;

    public BookingRoutes(ActorSystem<?> system, ActorRef<BookingRegistry.Command> bookingRegistryActor) {
        this.bookingRegistryActor = bookingRegistryActor;
        this.scheduler = system.scheduler();
        askTimeout = system.settings().config().getDuration("my-app.routes.ask-timeout");
    }

    private CompletionStage<BookingRegistry.GetShowResponse> getShow(Long id) {
        return AskPattern.ask(bookingRegistryActor, ref -> new BookingRegistry.GetShow(ref, id), askTimeout, scheduler);
    }

    public Route showRoute() {
        return pathPrefix("shows",
                () -> path(PathMatchers.longSegment(),
                        (Long showId) -> get(() -> onSuccess(getShow(showId),
                                show -> {
                                    if (show.show() != null)
                                        return complete(StatusCodes.OK, show.show(), Jackson.marshaller());
                                    return complete(StatusCodes.NOT_FOUND, "Show not exists with specified Id",
                                            Jackson.marshaller());
                                }))));
    }
}
