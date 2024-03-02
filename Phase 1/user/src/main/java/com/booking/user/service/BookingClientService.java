package com.booking.user.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.booking.user.exceptions.BookingNotFoundException;

@Service
public class BookingClientService implements IBookingClientService{
    private @NonNull String baseUrl = "http://host.docker.internal:8081/bookings";
    private final WebClient webClient;

    public BookingClientService() {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    private String delete(@NonNull String uri){
        // function to send http delete request to the uri
        return (
            webClient.delete()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String.class)
            .block()
        );
    }

    public String deleteBookingsByUserId(Long userId) throws BookingNotFoundException {
        String uri = "/" + Long.toString(userId);
        try{
            return this.delete(uri);
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND)
                throw new BookingNotFoundException("Booking not found for Specified User");
            throw ex;
        }
    }

    public String deleteAllBookings(){
        return this.delete("");
    }

}
