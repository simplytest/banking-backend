package com.simplytest.server.integration;

import com.simplytest.core.contracts.Contract;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerTestU3 {

    @Autowired
    private ContractRepository repository;

    @Autowired
    TestRestTemplate restTemplate;
    // Übung 2-3, Vertrag eines Kunden abrufen
    @Test
    public void getContractInformation() {
        String url = "/api/contracts";
        var dbContract = repository.save(new DBContract(createDummyContract()));
//        var customerContract = registerNewCustomer();
//
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, JWT.generate(dbContract.id()));
        headers.setContentType(MediaType.APPLICATION_JSON);
//
        HttpEntity<String> entity = new HttpEntity<>(headers);
//
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> Assertions.assertTrue(response.getBody().contains("Foo"))
        );
        Contract contract = Json.get().fromJson(response.getBody(), Contract.class);
        System.out.println(response.getBody());
        Assertions.assertAll(
                () -> Assertions.assertEquals("hanibal", contract.getCustomer().getFirstName()),
                () -> Assertions.assertEquals("lecter", contract.getCustomer().getLastName()),
                () -> Assertions.assertEquals("Mr Hyde Str", contract.getCustomer().getAddress().getStreet()),
                () -> Assertions.assertEquals("Gotham", contract.getCustomer().getAddress().getCity()),
                () -> Assertions.assertEquals("Germany", contract.getCustomer().getAddress().getCountry()),
                () ->Assertions.assertEquals("878765", contract.getCustomer().getAddress().getZipCode()),
                () ->Assertions.assertEquals("42", contract.getCustomer().getAddress().getHouse()),
                () ->Assertions.assertEquals("mey", contract.getCustomer().getAddress().getEmail())

        );
    }
    private CustomerData createCustomer() {
        String customer = "{\n" + //
                "  \"firstName\": \"hanibal\",\n" + //
                "  \"password\": \"demo\",\n" + //
                "  \"lastName\": \"lecter\",\n" + //
                "  \"address\": {\n" + //
                "    \"country\": \"Germany\",\n" + //
                "    \"zipCode\": \"878765\",\n" + //
                "    \"street\": \"Mr Hyde Str\",\n" + //
                "    \"house\": \"42\",\n" + //
                "    \"city\": \"Gotham\",\n" + //
                "    \"email\": \"mey\"\n" + //
                "  },\n" + //
                "  \"type\": \"Private\",\n" + //
                "  \"birthDay\": \"1984-06-27T14:12:49.117Z\",\n" + //
                "  \"ustNumber\": \"35252342\",\n" + //
                "  \"companyName\": \"Imortal Inc.\"\n" + //
                "}";
        return Json.get().fromJson(customer, CustomerData.class);
    }
    private Contract createDummyContract() {
        String dummy = """
                {
                  "id": {
                    "counter": 1,
                    "parent": 1,
                    "child": 0
                  },
                  "customer": {
                    "type": "com.simplytest.core.customers.CustomerPrivate",
                    "data": {
                      "birthDay": "1985-04-18",
                      "schufaScore": 0.0,
                      "transactionFee": 0.0,
                      "monthlyFee": 2.99,
                      "firstName": "Foo",
                      "lastName": "Bar",
                      "address": {
                        "country": "Deutschland",
                        "zipCode": "12345",
                        "street": "Some Street"
                      }
                    }
                  },
                  "passwordHash": "$2a$12$YkHRZthCXrh/OoXy7QG1suKccQ6pkIh8UlStfWPpbouqOVD0UcEyK",
                  "accounts": [
                    [
                      {
                        "counter": 1,
                        "parent": 1,
                        "child": 1
                      },
                      {
                        "type": "com.simplytest.core.accounts.AccountGiro",
                        "data": {
                          "sendLimit": 3000.0,
                          "dispoLimit": 0.0,
                          "dispoRate": 0.0,
                          "balance": 0.0,
                          "boundPeriod": 0.0,
                          "interestRate": 0.0
                        }
                      }
                    ]
                  ]
                }
                """;
        return Json.get().fromJson(dummy, Contract.class);
    }
}
