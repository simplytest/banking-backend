package com.simplytest.server.integrationTests.contractAPI;

import com.simplytest.core.contracts.Contract;
import com.simplytest.server.integrationTests.utils.ContractUtils;
import com.simplytest.server.json.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Aufgabe_4_2_3_retrieveAPI_Test {

    static final private String BASE_URL = "/api/contracts";

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void retrieveContractDataTest() {
        String endPointUrl = BASE_URL;
        var result = ContractUtils.registerNewCustomer(restTemplate, "SpringTest", "password");

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
