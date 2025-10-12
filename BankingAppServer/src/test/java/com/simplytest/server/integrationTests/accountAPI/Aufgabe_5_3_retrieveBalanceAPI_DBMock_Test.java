package com.simplytest.server.integrationTests.accountAPI;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Id;
import com.simplytest.core.accounts.AccountType;
import com.simplytest.core.contracts.Contract;
import com.simplytest.server.api.ContractController;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.integrationTests.utils.ContractUtils;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Aufgabe_5_3_retrieveBalanceAPI_DBMock_Test {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private ContractRepository contractDB;
    @MockitoBean
    private ContractController controller;

    private String jwtToken;
    private HttpHeaders headers = new HttpHeaders();
    private DBContract dbContract;
    final long contractID = 99;
    private double initialBalance = 1000.0;


    @BeforeEach
    public void setup() {
        jwtToken = JWT.generate(contractID);
        headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");

        var dummyContractWith = ContractUtils.createDemoContractFromDTO(new Id(contractID), "User", "demo", initialBalance);

        // Mocking der Datenbank und des Controllers registrieren
        registerMocks(dummyContractWith);

    }

    private void registerMocks(Contract contract) {
        dbContract = new DBContract(contract);

        // Mocking der Datenbank und des Controllers registrieren
        when(contractDB.findById(contract.getId().parent())).thenReturn(Optional.of(dbContract));
        when(contractDB.save(dbContract)).thenReturn(dbContract);

        // registerContract Funktion in Controller muss auch gemockt werden, da diese beim Hochfahren des Servers für Testdateninitialisierung aufgerufen wird
        // when(controller.registerContract(any(), any(), any()))
        //        .thenReturn(new Result<>(Optional.of(new ContractRegistrationResult(contractID, jwtToken)), Optional.empty()));
    }

    @Test
    public void getBalanceFromMockedDBTest() {

        long accountID = dbContract.value().getAccounts().entrySet().stream().filter(x -> x.getValue().getType() == AccountType.GiroAccount).findFirst().get().getKey().child();
        double balance = getBalanceForAccount(accountID);
        Assertions.assertEquals(initialBalance, balance);
    }

    private double getBalanceForAccount(long accountID) {
        // Die Konto-ID des Girokontos aus dem eingeschleusten Vertrag extrahieren
        String url = String.format("/api/accounts/%d/balance", accountID);

        // Kontostand abfragen
        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        System.out.println("Balance Response: " + response.getBody());

        // Prüfen, dass der Kontostand korrekt ist
        var balanceRespone = Json.get().fromJson(response.getBody(), new TypeToken<Result<Double, Error>>() {});
        Assertions.assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

        return balanceRespone.value();
    }


    @Test
    @DisplayName("Geld erhalten und Kontostand prüfen")
    public void transferMoneyFromMockedDBTest() {

        var amountReceived = 300.0;
        long accountID = dbContract.value().getAccounts().entrySet().stream().filter(x -> x.getValue().getType() == AccountType.GiroAccount).findFirst().get().getKey().child();

        // Mock muss mit aktualisiertem Kontostand neu registriert werden
        Contract contract = dbContract.value();
        contract.getAccount( new Id(contractID, accountID)).value().setBalance(initialBalance + amountReceived);
        registerMocks(contract);

        // Geld empfangen über API aufrufen
        String url = String.format(Locale.US,"/api/accounts/%d/receive?amount=%02.2f", accountID, amountReceived);
        var responseReceive = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        System.out.println("Receive response: " + responseReceive.getBody());

        var receiveResponse = Json.get().fromJson(responseReceive.getBody(), new TypeToken<Result<Boolean, Error>>() {});
        Assertions.assertEquals(HttpStatusCode.valueOf(200), responseReceive.getStatusCode());
        Assertions.assertEquals(true, receiveResponse.value());


        double balance = getBalanceForAccount(accountID);
        Assertions.assertEquals(initialBalance + amountReceived, balance);

    }


}
