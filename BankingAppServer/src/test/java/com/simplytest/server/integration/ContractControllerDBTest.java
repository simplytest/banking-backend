package com.simplytest.server.integration;

import com.google.common.reflect.TypeToken;
import com.simplytest.core.Id;
import com.simplytest.core.accounts.IAccount;
import com.simplytest.core.contracts.Contract;
import com.simplytest.core.utils.Pair;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.serviceObjects.ContractControllerObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.lang.reflect.Type;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerDBTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    ContractRepository repository;

    ContractControllerObject contractHelper;



    @Test
    public void getContractInformation() {
        contractHelper = new ContractControllerObject(restTemplate);
        String url = "/api/contracts";
        var dbContract = contractHelper.createDefaultCustomerInDB(repository);
        String jwt = JWT.generate(dbContract.id());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, JWT.generate(dbContract.id()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> Assertions.assertTrue(response.getBody().contains("Foo"))
        );
        Contract contract = Json.get().fromJson(response.getBody(), Contract.class);
        System.out.println(response.getBody());
        Assertions.assertAll(
                () -> Assertions.assertEquals("Foo", contract.getCustomer().getFirstName()),
                () -> Assertions.assertEquals("Bar", contract.getCustomer().getLastName())

        );
    }

    @Test
    public void addAccountToDBCreatedUser() {
        String url = "/api/contracts/accounts";
        var dummyContract = new DummyContract();
        var dbContract = repository.save(new DBContract(dummyContract.createDummyContract()));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, JWT.generate(dbContract.id()));
        headers.setContentType(MediaType.APPLICATION_JSON);
        String realEstate = """
                {
                  "repaymentRate": 500,
                  "amount": 35000
                }""";

        HttpEntity<String> entity = new HttpEntity<>(realEstate, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        // Pair<Id, IAccount>
        Type account = new TypeToken<Pair<Id, IAccount>>() {
        }.getType();
        final var parsed = (Pair<Id, IAccount>) Json.get().fromJson(response.getBody(), account);

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> Assertions.assertTrue(parsed.second().getType().toString().contains("RealEstate")),
                () -> Assertions.assertEquals(-35000, parsed.second().getBalance()),
                () -> Assertions.assertEquals(0, parsed.second().getInterestRate())
        );

        headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, JWT.generate(dbContract.id()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        entity = new HttpEntity<>(headers);

        ResponseEntity<String> contractResponse = restTemplate.exchange("/api/contracts", HttpMethod.GET, entity, String.class);
        Assertions.assertEquals(HttpStatus.OK, contractResponse.getStatusCode());

        Contract contract = Json.get().fromJson(contractResponse.getBody(), Contract.class);
        System.out.println(contractResponse.getBody());
        Assertions.assertAll(
                () -> Assertions.assertEquals("Foo", contract.getCustomer().getFirstName()),
                () -> Assertions.assertEquals("Bar", contract.getCustomer().getLastName()),
                () -> Assertions.assertTrue(contractResponse.getBody().contains("AccountGiro")),
                () -> Assertions.assertTrue(contractResponse.getBody().contains("AccountRealEstate"))
        );


        repository.findById(contract.getId().child()).ifPresent(retrievedContract -> {
            Assertions.assertAll(
                    () -> Assertions.assertTrue(retrievedContract.value().getAccounts().values().stream()
                            .anyMatch(acc -> acc.getType().toString().contains("AccountGiro"))),
                    () -> Assertions.assertTrue(retrievedContract.value().getAccounts().values().stream()
                            .anyMatch(acc -> acc.getType().toString().contains("AccountRealEstate")))
            );
        });
    }

    @Test
    public void testUserWithModifiedAccount() {
        var dummyContract = new DummyContract();
        var contract = dummyContract.createDummyContract();
        contract = dummyContract.addRealEstateToDummy(contract, 35000, 500);
        contract = dummyContract.changeCustomerData(contract, "Hanibal", "Lecter");
        var dbContract = repository.save(new DBContract(contract));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, JWT.generate(dbContract.id()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> contractResponse = restTemplate.exchange("/api/contracts", HttpMethod.GET, entity, String.class);
        Assertions.assertEquals(HttpStatus.OK, contractResponse.getStatusCode());

        Contract newContract = Json.get().fromJson(contractResponse.getBody(), Contract.class);
        System.out.println(contractResponse.getBody());
        Assertions.assertAll(
                () -> Assertions.assertEquals("Hanibal", newContract.getCustomer().getFirstName()),
                () -> Assertions.assertEquals("Lecter", newContract.getCustomer().getLastName()),
                () -> Assertions.assertTrue(contractResponse.getBody().contains("AccountGiro")),
                () -> Assertions.assertTrue(contractResponse.getBody().contains("AccountRealEstate"))
        );
    }
}
