package com.booking.wallet.services;


import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.env.Environment;
import jakarta.annotation.Resource;


import org.springframework.lang.NonNull;

@Service
public class UserClientService implements IUserClientService {
    
    // private @NonNull String baseUrl = "http://" + System.getenv("USER_SERVICE_HOST") + ":" + System.getenv("USER_SERVICE_PORT") + "/users";
    private @NonNull String baseUrl = "http://user-service:8080/users";
    private final WebClient webClient;

    public UserClientService() {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    private String get(@NonNull String uri){
        // function to send get request to the given uri
        return (
            webClient.get()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String.class)
            .block()
        );
    }

    @Override
    public String getUserById(Long userId){
        String uri = "/" + Long.toString(userId);
        return get(uri);
    };
}
