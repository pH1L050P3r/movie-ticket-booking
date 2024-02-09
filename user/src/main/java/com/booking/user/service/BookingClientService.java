package com.booking.user.service;

import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class BookingClientService implements IBookingClientService{
    private @NonNull String baseUrl = "http://host.docker.internal:8081/bookings";
    private final WebClient webClient;

    public BookingClientService() {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    private String delete(@NonNull String uri){
        return (
            webClient.delete()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String.class)
            .block()
        );
    }

    public String deleteBookingsByUserId(Long userId){
        String uri = "/" + Long.toString(userId);
        return this.delete(uri);
    }

    public String deleteAllBookings(){
        return this.delete("");
    }

}
