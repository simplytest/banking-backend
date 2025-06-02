package com.simplytest.server.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void happyPathLoginTest() {
        String url = "/api/contracts/login/0001";
        String password = "demo";
        ResponseEntity<String> response = restTemplate.postForEntity(url, password, String.class);
        System.out.println(response.getBody());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }
}
