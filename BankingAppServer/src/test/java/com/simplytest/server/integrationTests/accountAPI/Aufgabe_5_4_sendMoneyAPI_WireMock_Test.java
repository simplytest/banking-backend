package com.simplytest.server.integrationTests.accountAPI;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Id;
import com.simplytest.core.accounts.AccountType;
import com.simplytest.core.contracts.Contract;
import com.simplytest.server.api.ContractController;
import com.simplytest.server.apiData.Iban;
import com.simplytest.server.apiData.SendMoney;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.Locale;
import java.util.Optional;

import static org.mockito.Mockito.when;

@EnableWireMock(@ConfigureWireMock(portProperties = "localhost", port = 8081))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = {"validationurl = http://localhost:8081"})
public class Aufgabe_5_4_sendMoneyAPI_WireMock_Test {

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
    @DisplayName("Kontostand abfragen mit gemockter DB")
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
    @DisplayName("Geld mit gemockter IBAN Prüfung senden")
    public void sendMoneyWithWireMockTest() {

        long accountID = dbContract.value().getAccounts().entrySet().stream().filter(x -> x.getValue().getType() == AccountType.GiroAccount).findFirst().get().getKey().child();
        SendMoney sendPayload = new SendMoney( new Iban("DE45120300000000202053"), 250.0);
        // JSON Payload für die Geldüberweisung erstellen, da SendMoney nicht korrekt über SpringBoot serialisiert wird
        String body = Json.get().toJson(sendPayload);

        // WireMock konfigurieren, um die IBAN-Validierung zu simulieren
        WireMock.stubFor(WireMock.get("/validator/validate?iban=" + sendPayload.target().raw()).willReturn(WireMock.ok()));

        // Mock muss mit aktualisiertem Kontostand neu registriert werden
        Contract contract = dbContract.value();
        contract.getAccount( new Id(contractID, accountID)).value().setBalance(initialBalance - sendPayload.amount());
        registerMocks(contract);


        // Geldüberweisung an externe IBAN über API aufrufen
        String url = String.format(Locale.US,"/api/accounts/%d/sendexternal", accountID);
        var responseSend = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        System.out.println("Send response: " + responseSend.getBody());

        // Prüfen, dass die Überweisung erfolgreich war
        var sendResult = Json.get().fromJson(responseSend.getBody(), new TypeToken<Result<Boolean, Error>>() {});
        Assertions.assertEquals(HttpStatusCode.valueOf(200), responseSend.getStatusCode());
        Assertions.assertEquals(true, sendResult.value());

        // Kontostand erneut abfragen
        double balance = getBalanceForAccount(accountID);
        Assertions.assertEquals(initialBalance - sendPayload.amount(), balance);

    }


}
