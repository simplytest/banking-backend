package com.simplytest.server.integrationTests.utils;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.core.contracts.Contract;
import com.simplytest.core.customers.CustomerPrivate;
import com.simplytest.server.BankingServer;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import java.util.Locale;

public class ContractUtils {

    static final private String BASE_URL = "/api/contracts";

    public record RegisterResponse(String jwtToken, long contractId) {}

    public static RegisterResponse registerNewCustomer(TestRestTemplate restTemplate, String customerName, String password) {
        String endPointUrl = BASE_URL;

        // neuen Vertrag registrieren
        final var newCustomer = BankingServer.createDemoUser(customerName, password);
        var registerResponse = restTemplate.postForEntity(endPointUrl + "?initialBalance=" + "500", newCustomer, String.class);
        Assertions.assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        // deserialisiere die Antwort
        final TypeToken<?> registerResponseType = new TypeToken<Result<ContractRegistrationResult, com.simplytest.core.Error>>() {};
        @SuppressWarnings("unchecked")
        Result<ContractRegistrationResult, com.simplytest.core.Error> response = (Result<ContractRegistrationResult, Error>) Json.get().fromJson(registerResponse.getBody(), registerResponseType);
        Assertions.assertNotNull(response);

        return new RegisterResponse(response.value().JWT(), response.value().id());
    }

    public static DBContract registerNewContractInDB(ContractRepository contractDB, String firstName, String password, double initialBalance) {

        // Besonderheit der Implementierung der Businesslogik: zuerst zwingend einen leeren DB-Eintrag erzeugen, damit wir eine ID haben udn einen Lock darauf entsteht
        DBContract newDBEntity = contractDB.save( new DBContract());

        // Option 1: Erzeugen des Vertrags aus DTO
        var contract = ContractUtils.createDemoContractFromDTO( new Id(newDBEntity.id()), firstName, password, initialBalance);

        // Option 2: Erzeugen des Vertrags aus JSON String
        //var contract = ContractUtils.createDemoContractFromJSON(new Id(newDBEntity.id()).parent(), firstName, password, initialBalance);

        // Überschreibe den leeren DB-Eintrag mit dem neuen Vertrag
        return contractDB.save( new DBContract(contract));
    }

    public static Contract createDemoContractFromDTO(Id contractID, String firstName, String password, double initialBalance) {

        CustomerData customerData = BankingServer.createDemoUser(firstName, password);
        var customer =  new CustomerPrivate(customerData.firstName, customerData.lastName, customerData.birthDay);
        var contract = Contract.create(contractID, customer, customerData.password).value();
        contract.getAccounts().values().stream().findFirst().get().setBalance(initialBalance);
        return contract;
    }


    public static Contract createDemoContractFromJSON(Long contractID, String firstName, String password, double initialBalance) {
        String dummy = String.format(Locale.ENGLISH,  """
                {
                  "id": {
                    "counter": 1,
                    "parent": %d,
                    "child": 0
                  },
                  "customer": {
                    "type": "com.simplytest.core.customers.CustomerPrivate",
                    "data": {
                      "birthDay": "1985-04-18",
                      "schufaScore": 0.0,
                      "transactionFee": 0.0,
                      "monthlyFee": 2.99,
                      "firstName": "%s",
                      "lastName": "Last Name",
                      "address": {
                        "country": "Deutschland",
                        "zipCode": "12345",
                        "street": "Some Street"
                      }
                    }
                  },
                  "passwordHash": "%s",
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
                          "balance": %.2f,
                          "boundPeriod": 0.0,
                          "interestRate": 0.0
                        }
                      }
                    ]
                  ]
                }
                """,
                contractID, firstName, password, initialBalance);

        return Json.get().fromJson(dummy, Contract.class);
    }


}
