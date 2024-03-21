package com.example;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.concat;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.onSuccess;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.path;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;

public class BookingRoutes {
    private final ActorRef<BookingRegistry.Command> bookingRegistryActor;
    private final Duration askTimeout;
    private final Scheduler scheduler;
    private final static Logger log = LoggerFactory.getLogger(BookingRoutes.class);

    public BookingRoutes(ActorSystem<?> system, ActorRef<BookingRegistry.Command> bookingRegistryActor) {
        this.bookingRegistryActor = bookingRegistryActor;
        this.scheduler = system.scheduler();
        askTimeout = system.settings().config().getDuration("my-app.routes.ask-timeout");
    }

    private CompletionStage<BookingRegistry.GetShowResponse> getShow(Long id) {
        return AskPattern.ask(bookingRegistryActor, ref -> new BookingRegistry.GetShowRequest(ref, id), askTimeout,
                scheduler);
    }

    private CompletionStage<BookingRegistry.GetTheatreResponse> getTheatre(Long id) {
        return AskPattern.ask(bookingRegistryActor, ref -> new BookingRegistry.GetTheatreRequest(ref, id), askTimeout,
                scheduler);
    }

    private CompletionStage<BookingRegistry.GetTheatreAllShowsResponse> getTheatreAllShows(Long theatreId) {
        return AskPattern.ask(bookingRegistryActor,
                ref -> new BookingRegistry.GetTheatreAllShowsRequest(ref, theatreId), askTimeout, scheduler);
    }

    public Route showRoute() {
        return pathPrefix("shows",
                () -> concat(path(PathMatchers.longSegment(),
                        (Long showId) -> get(() -> onSuccess(getShow(showId),
                                show -> {
                                    if (show.show() != null)
                                        return complete(StatusCodes.OK, show.show(), Jackson.marshaller());
                                    return complete(StatusCodes.NOT_FOUND, "Show not exists with specified Id",
                                            Jackson.marshaller());
                                }))),
                        pathPrefix("theatre",
                                () -> path(PathMatchers.longSegment(),
                                        (Long theatreId) -> get(() -> onSuccess(getTheatreAllShows(theatreId),
                                                theatre -> {
                                                    return complete(StatusCodes.OK, theatre.shows(),
                                                            Jackson.marshaller());
                                                }))))));
    }

    public Route theatreRoute() {
        return pathPrefix("theatre",
                () -> path(PathMatchers.longSegment(),
                        (Long theatreId) -> get(() -> onSuccess(getTheatre(theatreId),
                                theatre -> {
                                    if (theatre.theatre() != null)
                                        return complete(StatusCodes.OK, theatre.theatre(), Jackson.marshaller());
                                    return complete(StatusCodes.NOT_FOUND, "Theatre not exists with specified Id",
                                            Jackson.marshaller());
                                }))));
    }

    public Route urlRoute() {
        return concat(showRoute(), theatreRoute());
    }

}