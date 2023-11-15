package com.simplytest.server.utils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.gson.reflect.TypeToken;
import com.simplytest.server.BankingServer;
import com.simplytest.server.json.Json;

public class APIUtil
{
    private static Optional<String> api = Optional.empty();

    private static String getEndpoint()
    {
        if (api.isPresent())
        {
            return api.get();
        }

        CompletableFuture<Integer> port = new CompletableFuture<>();

        var app = new SpringApplication(BankingServer.class);

        app.addListeners((WebServerInitializedEvent event) -> {
            port.complete(event.getWebServer().getPort());
        });

        app.run();

        try
        {
            api = Optional.of(String.format("http://localhost:%d/api", port.get()));
        } catch (Exception e)
        {
            assert (false);
        }

        return api.get();
    }

    public static ResponseEntity<String> request(String endpoint, String token,
            HttpMethod method, Object body)
    {
        var rest = new RestTemplate();

        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var entity = new HttpEntity<>(Json.get().toJson(body), headers);

        var result = rest.exchange(String.format("%s/%s", getEndpoint(), endpoint),
                method, entity, String.class);

        return result;
    }

    public static <R> R request(String endpoint, String token, HttpMethod method,
            Object body, TypeToken<?> type)
    {
        var result = request(endpoint, token, method, body);
        return (R) Json.get().fromJson(result.getBody(), type);
    }
}
