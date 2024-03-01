package com.booking.booking.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.lang.NonNull;

@Service
public class UserClientService implements IUserClientService {

    private @NonNull String baseUrl = "http://" + System.getProperty("USER_SERVICE_HOST") + ":"
            + System.getProperty("USER_SERVICE_PORT") + "/users";
    private final WebClient webClient;

    public UserClientService() {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    private String get(@NonNull String uri) {
        // function to send get request to the given uri
        return (webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block());
    }

    @Override
    public String getUserById(Long userId) {
        String uri = "/" + Long.toString(userId);
        return get(uri);
    };
}
