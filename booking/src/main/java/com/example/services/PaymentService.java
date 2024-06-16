package com.example.services;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class PaymentService {

  private PaymentService() {}

  private static final Integer PAYMENT_MAX_RETRY = 1;

  public static String refund(Long userId, Long amount, Http http) {
    return updateWallet(userId, amount, "credit", http);
  }

  public static String payment(Long userId, Long amount, Http http) {
    return updateWallet(userId, amount, "debit", http);
  }

  private static String updateWallet(
    Long userId,
    Long amount,
    String action,
    Http http
  ) {
    Integer timeOut = PAYMENT_MAX_RETRY;
    String url = "http://localhost:8082/wallets/" + Long.toString(userId);
    Map<String, Object> data = new HashMap<>();
    data.put("action", action);
    data.put("amount", Long.toString(amount));
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonData = "";
    try {
      jsonData = objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "FAIL";
    }
    try {
      while (timeOut-- != 0) {
        HttpRequest request = HttpRequest
          .PUT(url)
          .withEntity(ContentTypes.APPLICATION_JSON, jsonData);
        CompletionStage<HttpResponse> completion = http.singleRequest(request);
        HttpResponse response = completion.toCompletableFuture().join();
        if (response.status() == StatusCodes.OK) return "SUCCESS";
      }
    } catch (Exception e) {
      return "FAIL";
    }
    return "FAIL";
  }
}
