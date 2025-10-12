package com.simplytest.server.integrationTests.contractAPI;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Id;
import com.simplytest.core.accounts.AccountRealEstate;
import com.simplytest.core.accounts.IAccount;
import com.simplytest.core.contracts.Contract;
import com.simplytest.core.utils.Pair;
import com.simplytest.server.apiData.RealEstateAccount;
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
public class Aufgabe_5_2_addAccount_DB_Injection_Test {

    static final private String BASE_URL = "/api/contracts";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ContractRepository contractDB;


    @Test
    public void addImmoTilgungsKontoTest() {

        // (1) Neuen Vertrag direkt in Datenbank einschleusen
        final String firstName = "SpringTest";
        final double balance = 0.0;
        var result = ContractUtils.registerNewContractInDB(contractDB, firstName, "password", balance);

        var currentContractReference = contractDB.findById(result.id()).get();
        int initialNumberOFAccounts = currentContractReference.value().getAccounts().size();


        // (2) Füge dem Vertrag ein neues Tilgungskonto hinzu
        int repaymentRate = 500;
        int creditAmount = 200000;
        RealEstateAccount account = new RealEstateAccount(repaymentRate, creditAmount);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", JWT.generate(result.id()));

        var addAccountResponse = restTemplate.exchange(BASE_URL + "/accounts", HttpMethod.POST,  new HttpEntity<>(account, headers),  String.class);
        Assertions.assertEquals(HttpStatus.OK, addAccountResponse.getStatusCode());
        System.out.println("Account Info: " + addAccountResponse.getBody());

        // (3) Prüfe, dass das neue Konto mit richtigen Werten angelegt wurde
        var addResult = Json.get().fromJson(addAccountResponse.getBody(), new TypeToken<Pair<Id, IAccount>>(){} );
        Assertions.assertTrue(addResult.second().getType().name().contains("RealEstateAccount"));
        Assertions.assertEquals(-1*creditAmount, addResult.second().getBalance());
        Assertions.assertEquals(creditAmount, ((AccountRealEstate)addResult.second()).getCreditAmount() );

        // (4) Prüfe, dass das Konto zum Vertrag hinzugefügt wurde
        currentContractReference = contractDB.findById(result.id()).get();
        Assertions.assertEquals(initialNumberOFAccounts + 1, currentContractReference.value().getAccounts().size());
        Assertions.assertTrue(currentContractReference.value().getAccounts().values().stream().skip(1).findFirst().get().getType().name().contains("RealEstateAccount"));

        // (5) Gegenprüfung über API
        var contractInfoResponse = restTemplate.exchange(BASE_URL, HttpMethod.GET,  new HttpEntity<>(headers),  String.class);
        Assertions.assertEquals(HttpStatus.OK, contractInfoResponse.getStatusCode());
        System.out.println("Contract Info: " + contractInfoResponse.getBody());

        var contract = Json.get().fromJson(contractInfoResponse.getBody(), Contract.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(initialNumberOFAccounts + 1, contract.getAccounts().size()),
                () -> Assertions.assertTrue(contract.getAccounts().values().stream().skip(1).findFirst().get().getType().name().contains("RealEstateAccount")),
                () -> Assertions.assertEquals(-1*creditAmount, contract.getAccounts().values().stream().skip(1).findFirst().get().getBalance())
        );

    }


}
