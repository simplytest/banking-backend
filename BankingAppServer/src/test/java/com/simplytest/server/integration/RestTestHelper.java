package com.simplytest.server.integration;

import com.google.gson.Gson;

import com.google.gson.JsonParser;
import com.simplytest.server.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;


@TestComponent
public class RestTestHelper {
    @Autowired
    private TestRestTemplate restTemplate;
    private static final Gson gson = new Gson();

    public  String  login(long id, String pw){
        String path = "/api/contracts/login/" + id;
        var result =  restTemplate.postForEntity(path, pw, String.class);
        return JsonParser.parseString(result.getBody()).getAsJsonObject().get("result").getAsString();
    }
    public  ResponseEntity<String> executeCall(String path, String jwt, HttpMethod method) {

        HttpEntity<String> entity = null;

        if (jwt != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", jwt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            entity = new HttpEntity<>(headers);

        }

        return restTemplate.exchange(
                path,
                method,
                entity,
                String.class
        );

    }
    public  ResponseEntity<String> executePuttOrCall(String path, String jwt, HttpMethod method, Object body) {

        HttpEntity<String> entity = null;

        if (jwt != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", jwt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            entity = new HttpEntity<>(gson.toJson(body), headers);

        }

        return restTemplate.exchange(
                path,
                method,
                entity,
                String.class
        );

    }

    public <T> T getParsedResultFromResponseEntity(ResponseEntity<String> response, Class<T> clazz) {
        // Verwende direkt die Class<T> für die Deserialisierung
        return Json.get().fromJson(response.getBody(), clazz);
    }

    private ResponseEntity<String> executeCall(String path, HttpMethod method) {

        return executeCall(path, null, method);

    }
}