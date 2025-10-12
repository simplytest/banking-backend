package com.simplytest.server.integrationTests.contractAPI;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.simplytest.server.BankingServer;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.json.Json;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import com.simplytest.core.Error;
import org.springframework.http.MediaType;

import java.time.Year;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Aufgabe_4_2_2_registerAPI_Test {

    final private String BASE_URL = "/api/contracts";

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void registerTestWithValidData() {
        String endPointUrl = BASE_URL + "?initialBalance=" + "500";

        final String payload =
                """
                {
                    "firstName": "SpringTest",
                    "password": "password",
                    "lastName": "User",
                    "address": {
                      "country": "Germany",
                      "zipCode": "12345",
                      "street": "New Street",
                      "house": "1",
                      "city": "My Cit",
                      "email": "mail@test.de"
                    },
                    "type": "Private",
                    "birthDay": "2005-10-11T08:36:17.197Z"
                  }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var registerResponse = restTemplate.postForEntity(endPointUrl, new HttpEntity<>(payload, headers), String.class);
        System.out.println(registerResponse);

        Assertions.assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        // deserialisiere die Antwort
        JsonObject response = Json.get().fromJson(registerResponse.getBody(), JsonObject.class);

        // prüfe, dass im Ergebnis ein JWT Token enthalten ist
        String jwt = response.getAsJsonObject("result").get("JWT").getAsString();
        Assertions.assertTrue(!jwt.isEmpty());
        System.out.println("JWT:" + jwt);

        // prüfe, dass im Ergebnis die neue Contract ID enthalten ist
        int contractId = response.getAsJsonObject("result").get("id").getAsInt();
        Assertions.assertTrue(contractId > 0);
        System.out.println("Contract ID: " + contractId);

    }


    @Test
    public void registerTestWithValidDataOptimized() {
        String endPointUrl = BASE_URL + "?initialBalance=" + "500";

        final var newCustomer = BankingServer.createDemoUser("SpringTest", "password");

        var registerResponse = restTemplate.postForEntity(endPointUrl, newCustomer, String.class);
        System.out.println(registerResponse);

        Assertions.assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        // deserialisiere die Antwort
        final TypeToken<Result<ContractRegistrationResult, Error>> registerResponseType = new TypeToken<>() {};
        Result<ContractRegistrationResult, Error> response = Json.get().fromJson(registerResponse.getBody(), registerResponseType);

        // prüfe, dass im Ergebnis ein JWT Token enthalten ist
        Assertions.assertTrue(!response.value().JWT().isEmpty());
        System.out.println("JWT:" + response.value().JWT());

        // prüfe, dass im Ergebnis die neue Contract ID enthalten ist
        Assertions.assertTrue(response.value().id() > 0);
        System.out.println("Contract ID: " + response.value().id());

    }


    @Test
    public void registerTestWithUnderAgeCustomer() {

        String endPointUrl = BASE_URL + "?initialBalance=" + "500";

        final var newCustomer = BankingServer.createDemoUser("SpringTest", "password");
        newCustomer.birthDay.setYear (Year.now().getValue());

        var registerResponse = restTemplate.postForEntity(endPointUrl, newCustomer, String.class);
        System.out.println(registerResponse);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, registerResponse.getStatusCode());

        // deserialisiere die Antwort
        final TypeToken<Result<ContractRegistrationResult, Error>> registerResponseType = new TypeToken<>() {};
        Result<ContractRegistrationResult, Error> response = Json.get().fromJson(registerResponse.getBody(), registerResponseType);

        // prüfe, dass im Ergebnis ein JWT Token enthalten ist
        Assertions.assertTrue(!response.error().error().toString().isEmpty());
        System.out.println("Expected error:" + response.error().error());

        // prüfe, dass im Ergebnis die neue Contract ID enthalten ist
        Assertions.assertTrue(response.error().error().toString().contains("Underage"));

    }


}
