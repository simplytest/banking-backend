package com.simplytest.server.integration;

import com.simplytest.core.contracts.Contract;
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
public class ContractControllerDBTest {


    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ContractRepository contractRepository;

    private DBContract createCustomerInDatabase() {
        return contractRepository.save(new DBContract(createDummyContract()));
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

    @Test
    public void newCustomerInDatabase() {
        String url = "/api/contracts";
        var custommerContract = createCustomerInDatabase();
        String jwtToken = JWT.generate(custommerContract.id());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println(response.getBody());

    }

}
