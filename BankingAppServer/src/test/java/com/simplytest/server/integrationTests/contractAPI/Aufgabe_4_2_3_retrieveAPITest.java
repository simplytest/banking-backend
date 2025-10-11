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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Aufgabe_4_2_3_retrieveAPITest {

    static final private String BASE_URL = "/api/contracts";

    @Autowired
    private TestRestTemplate restTemplate;



    public record RegisterResponse(String jwtToken, long contractId) {}

    public static RegisterResponse registerNewCustomer(TestRestTemplate restTemplate, String customerName, String password) {
        String endPointUrl = BASE_URL;

        // neuen Vertrag registrieren
        final var newCustomer = BankingServer.createDemoUser(customerName, password);
        var registerResponse = restTemplate.postForEntity(endPointUrl + "?initialBalance=" + "500", newCustomer, String.class);
        Assertions.assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        // deserialisiere die Antwort
        final TypeToken<?> registerResponseType = new TypeToken<Result<ContractRegistrationResult, Error>>() {};
        @SuppressWarnings("unchecked")
        Result<ContractRegistrationResult, Error> response = (Result<ContractRegistrationResult, Error>) Json.get().fromJson(registerResponse.getBody(), registerResponseType);
        Assertions.assertNotNull(response);

        return new RegisterResponse(response.value().JWT(), response.value().id());
    }


    @Test
    public void retrieveContractDataTest() {
        String endPointUrl = BASE_URL;
        var result = registerNewCustomer(restTemplate, "SpringTest", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", result.jwtToken());

        var contractInfoResponse = restTemplate.exchange(endPointUrl, HttpMethod.GET,  new HttpEntity<>(headers),  String.class);
        Assertions.assertEquals(HttpStatus.OK, contractInfoResponse.getStatusCode());
        System.out.println(contractInfoResponse.getBody());

        var contract = Json.get().fromJson(contractInfoResponse.getBody(), Contract.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(result.contractId(), contract.getId().parent()),
                () -> Assertions.assertEquals("SpringTest", contract.getCustomer().getFirstName()),
                () -> Assertions.assertEquals(500.0, contract.getAccounts().values().stream().findFirst().get().getBalance())
        );

    }



}
