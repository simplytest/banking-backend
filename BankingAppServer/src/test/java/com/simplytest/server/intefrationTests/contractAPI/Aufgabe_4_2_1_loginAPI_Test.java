package com.simplytest.server.intefrationTests.contractAPI;

import com.google.gson.JsonObject;
import com.simplytest.server.json.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Aufgabe_4_2_1_loginAPI_Test {

    final private String BASE_URL = "/api/contracts";
    final private String LOGIN_URL = BASE_URL + "/login";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testLoginWithValidData() {
        String endPointUrl = LOGIN_URL + "/0001";
        String password = "demo";

        var loginResponse = restTemplate.postForEntity(endPointUrl, password, String.class);
        System.out.println(loginResponse.getBody());

        Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);

        Assertions.assertNotNull(response.get("result"));
        Assertions.assertTrue(response.get("result").isJsonPrimitive());

        var jwtToken = response.get("result").getAsString();
        Assertions.assertTrue(!jwtToken.isEmpty());
        System.out.println(jwtToken);
    }
}
