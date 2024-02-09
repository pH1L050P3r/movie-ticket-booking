package com.booking.booking.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.booking.booking.enums.Action;

import org.springframework.lang.NonNull;



@Service
public class WalletClientService implements IWalletClientService {
    private @NonNull String baseUrl = "http://host.docker.internal:8082/wallets";
    private final WebClient webClient;

    public WalletClientService() {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    private String put(@NonNull String uri, @NonNull Map<String, String> body){
        return (
            webClient.put()
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(body))
            .retrieve()
            .bodyToMono(String.class)
            .block()
        );
    }

    private String get(@NonNull String uri){
        return (
            webClient.get()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String.class)
            .block()
        );
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

    @Override
    public String fetchWalletByUserId(Long userId){
        String uri = "/" + Long.toString(userId);
        return this.get(uri);
    }

    @Override
    public String updateUserWalletMoney(Long amount, Long userId, Action action) {
        String uri = "/" + Long.toString(userId);
        Map<String, String> data = new HashMap<String, String>();
        data.put("action", action.toString());
        data.put("amount", Long.toString(amount));
        return this.put(uri, data);
    }

    @Override
    public String deleteUserById(Long userId){
        String uri = "/" + Long.toString(userId);
        return this.delete(uri);
    }

    @Override
    public String deleteAllUser(Long userId){
        String uri = baseUrl;
        return this.delete(uri);
    }
}
