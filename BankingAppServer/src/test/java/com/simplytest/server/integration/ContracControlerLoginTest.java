package com.simplytest.server.integration;

import com.google.gson.JsonObject;
import com.simplytest.server.json.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContracControlerLoginTest {
    @Autowired
    private TestRestTemplate restTemplate;

    String endPointUrl = "";

    @BeforeEach
    public void setup() {
        endPointUrl = "/api/contracts/login/0001";
    }

    @Test
    public void happyPathUserLoginSuccessful() {
        String password = "demo";
        var loginResponse = restTemplate.postForEntity(endPointUrl, password, String.class);
        System.out.println(loginResponse);
        Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);
        Assertions.assertNotNull(response.get("result"));
        Assertions.assertTrue(response.get("result").isJsonPrimitive());
        var myJWT = response.get("result").getAsString();
        Assertions.assertFalse(myJWT.isEmpty());
        System.out.println(myJWT);
    }

    // Login with bad credentials
    @Test
    public void loginWithExistingCustomerBadCredentials() {
        String password = "nix";
        var loginResponse = restTemplate.postForEntity(endPointUrl, password, String.class);
        System.out.println(loginResponse);
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);
        // Gebündelte Assertions
        JsonObject error = response.getAsJsonObject("error");
        Assertions.assertAll(
                // Jede Assertion als Lambda
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST, loginResponse.getStatusCode()),
                () -> Assertions.assertNotNull(response.get("error")),
                () -> Assertions.assertEquals("BadCredentials", error.get("error").getAsString())
        );
    }

    // Login with non-existing contract ID 4444
    @Test
    public void loginTestFailedContractNotFound() {
        var url = "/api/contracts/login/4444";
        var pwd = "falsch";

        var response = restTemplate.postForEntity(url, pwd, String.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        System.out.println(response.getBody());
        JsonObject result = Json.get().fromJson(response.getBody(), JsonObject.class);
        Assertions.assertEquals("Not Found", result.get("error").getAsString());
    }
}
