package com.example;

import static akka.http.javadsl.server.Directives.*;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Directives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import com.example.BookingRegistry.CreateBookingRequestBody;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookingRoutes {

  private final ActorRef<BookingRegistry.Command> bookingRegistryActor;
  private final Duration askTimeout;
  private final Scheduler scheduler;
  private static final Logger log = LoggerFactory.getLogger(
    BookingRoutes.class
  );

  public BookingRoutes(
    ActorSystem<?> system,
    ActorRef<BookingRegistry.Command> bookingRegistryActor
  ) {
    this.bookingRegistryActor = bookingRegistryActor;
    this.scheduler = system.scheduler();
    askTimeout =
      system.settings().config().getDuration("my-app.routes.ask-timeout");
  }

  private CompletionStage<BookingRegistry.GetShowResponse> getShow(Long id) {
    return AskPattern.ask(
      bookingRegistryActor,
      ref -> new BookingRegistry.GetShowRequest(ref, id),
      askTimeout,
      scheduler
    );
  }

  private CompletionStage<BookingRegistry.GetTheatreResponse> getTheatre(
    Long id
  ) {
    return AskPattern.ask(
      bookingRegistryActor,
      ref -> new BookingRegistry.GetTheatreRequest(ref, id),
      askTimeout,
      scheduler
    );
  }

  private CompletionStage<BookingRegistry.GetAllTheatresResponse> getAllTheatres() {
    return AskPattern.ask(
      bookingRegistryActor,
      BookingRegistry.GetAllTheatresRequest::new,
      askTimeout,
      scheduler
    );
  }

  private CompletionStage<BookingRegistry.GetTheatreAllShowsResponse> getTheatreAllShows(
    Long theatreId
  ) {
    return AskPattern.ask(
      bookingRegistryActor,
      ref -> new BookingRegistry.GetTheatreAllShowsRequest(ref, theatreId),
      askTimeout,
      scheduler
    );
  }

  private CompletionStage<BookingRegistry.CreateBookingResponse> createBooking(
    CreateBookingRequestBody requestBody
  ) {
    return AskPattern.ask(
      bookingRegistryActor,
      ref -> new BookingRegistry.CreateBookingRequest(ref, requestBody),
      askTimeout,
      scheduler
    );
  }

  private CompletionStage<BookingRegistry.GetUserAllBookingsResponse> getUserAllBookings(
    Long userId
  ) {
    return AskPattern.ask(
      bookingRegistryActor,
      ref -> new BookingRegistry.GetUserAllBookingsRequest(ref, userId),
      askTimeout,
      scheduler
    );
  }

  private CompletionStage<BookingRegistry.DeleteUserAllBookingsResponse> deleteUserAllBookings(
    Long userId
  ) {
    return AskPattern.ask(
      bookingRegistryActor,
      ref -> new BookingRegistry.DeleteUserAllBookingsRequest(ref, userId),
      askTimeout,
      scheduler
    );
  }

  private CompletionStage<BookingRegistry.DeleteUserShowBookingsResponse> deleteUserShowBookings(
    Long userId,
    Long showId
  ) {
    log.info("I am here");
    return AskPattern.ask(
      bookingRegistryActor,
      ref ->
        new BookingRegistry.DeleteUserShowBookingsRequest(ref, userId, showId),
      askTimeout,
      scheduler
    );
  }

  public Route showRoute() {
    return pathPrefix(
      "shows",
      () ->
        concat(
          path(
            PathMatchers.longSegment(),
            (Long showId) ->
              get(() ->
                onSuccess(
                  getShow(showId),
                  show -> {
                    if (show.statusCode() == StatusCodes.OK) return complete(
                      show.statusCode(),
                      show.body(),
                      Jackson.marshaller()
                    );
                    return complete(
                      show.statusCode(),
                      show.message(),
                      Jackson.marshaller()
                    );
                  }
                )
              )
          ),
          pathPrefix(
            "theatres",
            () ->
              path(
                PathMatchers.longSegment(),
                (Long theatreId) ->
                  get(() ->
                    onSuccess(
                      getTheatreAllShows(theatreId),
                      theatre ->
                        complete(
                          theatre.statusCode(),
                          theatre.body(),
                          Jackson.marshaller()
                        )
                    )
                  )
              )
          )
        )
    );
  }

  public Route theatreRoute() {
    return pathPrefix(
      "theatres",
      () ->
        concat(
          path(
            PathMatchers.longSegment(),
            (Long theatreId) ->
              get(() ->
                onSuccess(
                  getTheatre(theatreId),
                  theatre -> {
                    if (theatre.statusCode() == StatusCodes.OK) return complete(
                      theatre.statusCode(),
                      theatre.body(),
                      Jackson.marshaller()
                    );
                    return complete(
                      theatre.statusCode(),
                      theatre.message(),
                      Jackson.marshaller()
                    );
                  }
                )
              )
          ),
          pathEnd(() ->
            get(() ->
              onSuccess(
                getAllTheatres(),
                theatres ->
                  complete(
                    theatres.statusCode(),
                    theatres.body(),
                    Jackson.marshaller()
                  )
              )
            )
          )
        )
    );
  }

  public Route bookingRoute() {
    return pathPrefix(
      "bookings",
      () ->
        concat(
          pathEnd(() ->
            concat(
              get(() -> complete("GET request received for /booking")),
              post(() ->
                entity(
                  Jackson.unmarshaller(
                    BookingRegistry.CreateBookingRequestBody.class
                  ),
                  bookingBody ->
                    onSuccess(
                      createBooking(bookingBody),
                      performed ->
                        complete(
                          performed.statusCode(),
                          performed.body(),
                          Jackson.marshaller()
                        )
                    )
                )
              )
            )
          ),
          pathPrefix(
            "users",
            () ->
              Directives.route(
                path(
                  PathMatchers
                    .longSegment()
                    .slash("shows")
                    .slash(PathMatchers.longSegment()),
                  (Long userId, Long showId) ->
                    delete(() ->
                      onSuccess(
                        deleteUserShowBookings(userId, showId),
                        theatre -> complete(theatre.statusCode())
                      )
                    )
                ),
                path(
                  PathMatchers.longSegment(),
                  (Long userId) ->
                    pathEnd(() ->
                      concat(
                        get(() ->
                          onSuccess(
                            getUserAllBookings(userId),
                            theatre ->
                              complete(
                                theatre.statusCode(),
                                theatre.body(),
                                Jackson.marshaller()
                              )
                          )
                        ),
                        delete(() ->
                          onSuccess(
                            deleteUserAllBookings(userId),
                            theatre -> complete(theatre.statusCode())
                          )
                        )
                      )
                    )
                )
              )
          )
        )
    );
  }

  public Route urlRoute() {
    return concat(showRoute(), theatreRoute(), bookingRoute());
  }
}
