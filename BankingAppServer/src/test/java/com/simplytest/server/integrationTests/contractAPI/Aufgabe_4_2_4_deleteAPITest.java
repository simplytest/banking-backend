package com.simplytest.server.integrationTests.contractAPI;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.core.contracts.Contract;
import com.simplytest.server.BankingServer;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.json.Json;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static com.simplytest.server.integrationTests.contractAPI.Aufgabe_4_2_3_retrieveAPITest.registerNewCustomer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Aufgabe_4_2_4_deleteAPITest {

    static final private String BASE_URL = "/api/contracts";

    @Autowired
    private TestRestTemplate restTemplate;



    public record RegisterResponse(String jwtToken, long contractId) {}


    @Test
    public void deleteContractTest() {
        String endPointUrl = BASE_URL;
        var result = registerNewCustomer(restTemplate, "SpringTest", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", result.jwtToken());

        var contractDeleteResponse = restTemplate.exchange(endPointUrl, HttpMethod.DELETE,  new HttpEntity<>(headers),  String.class);
        Assertions.assertEquals(HttpStatus.OK, contractDeleteResponse.getStatusCode());
        System.out.println(contractDeleteResponse.getBody());

        final TypeToken<?> deleteResponseType = new TypeToken<Result<Boolean, Error>>() {};
        @SuppressWarnings("unchecked")
        var response = (Result<Boolean, Error>) Json.get().fromJson(contractDeleteResponse.getBody(), deleteResponseType);

        Assertions.assertTrue(response.successful());
        Assertions.assertEquals("true", response.value().toString());

        // prüfen, dass der gelöschte Vertrag nicht mehr abgerufen werden kann
        var contractInfoResponse = restTemplate.exchange(endPointUrl, HttpMethod.GET,  new HttpEntity<>(headers),  String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, contractInfoResponse.getStatusCode());

    }



}
