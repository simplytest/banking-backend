package com.simplytest.server.intefrationTests.contractAPI;

import java.time.Year;
import java.util.Calendar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplytest.server.BankingServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Aufgabe_4_2_2_registerAPI_Jackson_Test {

    final private String BASE_URL = "/api/contracts";

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void registerTestWithValidData() throws Exception {
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

        // deserialisiere die Antwort mit Jackson JsonNode
        JsonNode response = objectMapper.readTree(registerResponse.getBody());

        // prüfe, dass im Ergebnis ein JWT Token enthalten ist
        String jwt = response.path("result").path("JWT").asText();
        Assertions.assertFalse(jwt.isEmpty());
        System.out.println("JWT:" + jwt);

        // prüfe, dass im Ergebnis die neue Contract ID enthalten ist
        int contractId = response.path("result").path("id").asInt();
        Assertions.assertTrue(contractId > 0);
        System.out.println("Contract ID: " + contractId);
    }


    @Test
    public void registerTestWithValidDataOptimized() throws Exception {
        String endPointUrl = BASE_URL + "?initialBalance=" + "500";

        final var newCustomer = BankingServer.createDemoUser("SpringTest", "password");

        var registerResponse = restTemplate.postForEntity(endPointUrl, newCustomer, String.class);
        System.out.println(registerResponse);

        Assertions.assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        // deserialisiere die Antwort mit Jackson JsonNode
        JsonNode response = objectMapper.readTree(registerResponse.getBody());

        // prüfe, dass im Ergebnis ein JWT Token enthalten ist
        String jwt = response.path("result").path("JWT").asText();
        Assertions.assertFalse(jwt.isEmpty());
        System.out.println("JWT:" + jwt);

        // prüfe, dass im Ergebnis die neue Contract ID enthalten ist
        int contractId = response.path("result").path("id").asInt();
        Assertions.assertTrue(contractId > 0);
        System.out.println("Contract ID: " + contractId);
    }


    @Test
    public void registerTestWithUnderAgeCustomer() throws Exception {

        String endPointUrl = BASE_URL + "?initialBalance=" + "500";

        final var newCustomer = BankingServer.createDemoUser("SpringTest", "password");
        var cal = Calendar.getInstance();
        cal.setTime(newCustomer.birthDay);
        cal.set(Calendar.YEAR, Year.now().getValue());
        newCustomer.birthDay = cal.getTime();

        var registerResponse = restTemplate.postForEntity(endPointUrl, newCustomer, String.class);
        System.out.println(registerResponse);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, registerResponse.getStatusCode());

        // deserialisiere die Antwort mit Jackson JsonNode
        JsonNode response = objectMapper.readTree(registerResponse.getBody());

        // prüfe, dass im Ergebnis ein Fehler enthalten ist
        String errorText = response.path("error").path("error").asText();
        Assertions.assertFalse(errorText.isEmpty());
        System.out.println("Expected error:" + errorText);

        // prüfe, dass der Fehler "Underage" enthält
        Assertions.assertTrue(errorText.contains("Underage"));
    }
}
