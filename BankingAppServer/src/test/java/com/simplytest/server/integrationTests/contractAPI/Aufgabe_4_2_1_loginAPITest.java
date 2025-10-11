package com.simplytest.server.integrationTests.contractAPI;

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
public class Aufgabe_4_2_1_loginAPITest {

    final private String BASE_URL = "/api/contracts";
    final private String LOGIN_URL = BASE_URL + "/login";

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void loginTestWithValidData() {
        String endPointUrl = LOGIN_URL + "/0001";
        String password = "demo";

        var loginResponse = restTemplate.postForEntity(endPointUrl, password, String.class);
        System.out.println(loginResponse);

        Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        // prüfe, dass im Ergebnis ein JWT Token enthalten ist
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);
        Assertions.assertNotNull(response.get("result"));
        Assertions.assertTrue(response.get("result").isJsonPrimitive());

        var jwtToken = response.get("result").getAsString();
        Assertions.assertTrue(!jwtToken.isEmpty());
        System.out.println(jwtToken);
    }

    // Validieren des Logins
    // mit gültigen Daten, was können wir auswerten?
    // Login mit falschen Daten, welche Fehlermeldung wird zurückgegeben?
    // Login mit einem nicht existierenden Kunden, was passiert?

    @Test
    public void loginTestInvalidPasswordFailed() {
        String endPointUrl = LOGIN_URL + "/0001";
        String password = "falsch";

        var loginResponse = restTemplate.postForEntity(endPointUrl, password, String.class);
        System.out.println(loginResponse);

        // parse die Antwort als generisches JSON Objekt (1)
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);

        // prüfe, dass im Ergebnis die richtige Fehlermeldung enthalten ist
        // gebündelte Assertions
        Assertions.assertAll(
                // Jede Assertion als Lambda
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST, loginResponse.getStatusCode()),
                () -> Assertions.assertEquals(Error.BadCredentials.toString(), response.get("error").getAsJsonObject().get("error").getAsString())
        );
    }


    @Test
    public void loginTestInvalidPasswordFailedOptimized() {
        var url = LOGIN_URL + "/0001";
        var pwd = "falsch";

        var loginResponse = restTemplate.postForEntity(url, pwd, String.class);
        System.out.println(loginResponse.getBody());

        // parse die Antwort durch die Deserialisierung in das Result Objekt (2)
        var result = Json.get().fromJson(loginResponse.getBody(), Result.class);

        // prüfe, dass im Ergebnis die richtige Fehlermeldung enthalten ist
        Assertions.assertTrue(loginResponse.getStatusCode().is4xxClientError());
        Assertions.assertTrue(Error.BadCredentials.toString().equals(result.error().error().toString()));

    }



    @Test
    public void loginTestInvalidUserFailed() {
        var url = LOGIN_URL +  "/1234";
        var pwd = "falsch";

        var loginResponse = restTemplate.postForEntity(url, pwd, String.class);
        System.out.println(loginResponse.getBody());

        Assertions.assertEquals(HttpStatus.NOT_FOUND, loginResponse.getStatusCode());
    }

}
