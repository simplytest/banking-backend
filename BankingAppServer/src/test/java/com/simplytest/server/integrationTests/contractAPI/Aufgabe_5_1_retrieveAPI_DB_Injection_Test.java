package com.simplytest.server.integrationTests.contractAPI;

import com.simplytest.core.contracts.Contract;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.integrationTests.utils.ContractUtils;
import com.simplytest.server.json.Json;
import com.simplytest.server.repo.ContractRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;




@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Aufgabe_5_1_retrieveAPI_DB_Injection_Test {

    static final private String BASE_URL = "/api/contracts";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ContractRepository contractDB;


    @Test
    public void retrieveContractDataTest() {
        final String endPointUrl = BASE_URL;

        // (1) Neuen Vertrag direkt in Datenbank einschleusen
        final String firstName = "SpringTest";
        final double balance = 0.0;


        var result = ContractUtils.registerNewContractInDB(contractDB, firstName, "password", balance);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", JWT.generate(result.id()));

        // (2) Rufe die Vertragsdaten über die API ab
        var contractInfoResponse = restTemplate.exchange(endPointUrl, HttpMethod.GET,  new HttpEntity<>(headers),  String.class);
        Assertions.assertEquals(HttpStatus.OK, contractInfoResponse.getStatusCode());
        System.out.println(contractInfoResponse.getBody());

        var contract = Json.get().fromJson(contractInfoResponse.getBody(), Contract.class);

        // (3) Prüfe, dass die Vertragsdaten korrekt sind
        Assertions.assertAll(
                () -> Assertions.assertEquals(result.id(), contract.getId().parent()),
                () -> Assertions.assertEquals(firstName, contract.getCustomer().getFirstName()),
                () -> Assertions.assertEquals(balance, contract.getAccounts().values().stream().findFirst().get().getBalance())
        );

    }


}
