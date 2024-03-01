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

    private @NonNull String baseUrl = "http://" + System.getenv("WALLET_SERVICE_HOST") + ":"
            + System.getenv("WALLET_SERVICE_PORT");
    private final WebClient webClient;

    public WalletClientService() {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    private String put(@NonNull String uri, @NonNull Map<String, String> body) {
        // function to send http put request with the given body to the uri
        return (webClient.put()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .block());
    }

    @Override
    public String updateByUserId(Long amount, Long walletId, Action action) {
        String uri = "/wallets/" + Long.toString(walletId);
        // creating body for sending in put request body
        Map<String, String> data = new HashMap<String, String>();
        data.put("action", action.toString());
        data.put("amount", Long.toString(amount));
        return this.put(uri, data);
    }
}
