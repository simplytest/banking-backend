package com.simplytest.server.integration;

import com.google.gson.JsonObject;
import com.simplytest.core.Error;
import com.simplytest.server.json.Json;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerLoginTest {
    @Autowired
    private TestRestTemplate restTemplate;

    String baseUrl = "/api/contracts/login/";

    @Test
    public void happyPathLoginTest() {
        String url = baseUrl + "0001";
        String password = "demo";
        var loginResponse = restTemplate.postForEntity(url, password, String.class);
        System.out.println(loginResponse);
        Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        // prüfe, dass im Ergebnis ein JWT Token enthalten ist
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);
        Assertions.assertNotNull(response.get("result"));
        Assertions.assertTrue(response.get("result").isJsonPrimitive());
        var myJWT = response.get("result").getAsString();
        Assertions.assertFalse(myJWT.isEmpty());
        System.out.println(myJWT);

    }

    @Test
    public void loginWithExistingCustomerBadCredentials() {
        String endPointUrl = baseUrl + "0001";
        String password = "nix";
        var loginResponse = restTemplate.postForEntity(endPointUrl, password, String.class);
        System.out.println(loginResponse);
        //Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        // prüfe, dass im Ergebnis ein JWT Token enthalten ist
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);
        // Gebündelte Assertions

        Assertions.assertAll(
                // Jede Assertion als Lambda
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST, loginResponse.getStatusCode()),
                () -> Assertions.assertNotNull(response.get("error"))
        );
    }

    @Test
    public void loginTestInvalidPasswordFailed() {
        var url = baseUrl + "1";
        var pwd = "falsch";

        var response = restTemplate.postForEntity(url, pwd, String.class);


        var result = Json.get().fromJson(response.getBody(), Result.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertTrue(Error.BadCredentials.toString().equals(result.error().error().toString()));

        System.out.println(response.getBody());
    }

    @Test
    public void loginTestInvalidCredentialsFailed() {
        var url = baseUrl + "1234";
        var pwd = "falsch";

        var response = restTemplate.postForEntity(url, pwd, String.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        System.out.println(response.getBody());
    }
}
