package com.simplytest.server.utils;

import com.google.common.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.json.Json;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

public class AccountHandler {

    public double checkBalance(TestRestTemplate restTemplate, Id accountId, double expectedBalance, String jwtToken) {
        String url = String.format("/api/accounts/%d/balance", accountId.child());
        var header = new HttpHeaders();
        header.set(HttpHeaders.AUTHORIZATION, jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(header);

        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println(response.getBody());
        Type typeGetAccBalance = new TypeToken<Result<Double, com.simplytest.core.Error>>() {
        }.getType();
        var balanceRespone = (Result<Double, Error>) Json.get().fromJson(response.getBody(), typeGetAccBalance);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        Assertions.assertEquals(expectedBalance, balanceRespone.value());
        return balanceRespone.value();
    }
}
