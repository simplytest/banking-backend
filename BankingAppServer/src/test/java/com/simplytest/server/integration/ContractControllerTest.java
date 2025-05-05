package com.simplytest.server.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void happyPathUserLoginSuccessful() {
        String url = "/api/contracts/login/0001";
        String password = "demo";
        var loginResponse = restTemplate.postForEntity(url, password, String.class);
        System.out.println(loginResponse);
        Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    }

    // Login with bad credentials

    // Login with non existing contract ID 4444
}
