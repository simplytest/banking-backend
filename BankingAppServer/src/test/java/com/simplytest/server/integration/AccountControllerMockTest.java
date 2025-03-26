package com.simplytest.server.integration;

import com.google.common.reflect.TypeToken;
import com.simplytest.core.Id;
import com.simplytest.server.api.ContractController;
import com.simplytest.server.apiData.*;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.AccountHandler;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.simplytest.core.Error;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerMockTest {
    @Autowired
    TestRestTemplate restTemplate;

    @MockitoBean
    ContractRepository contractRepository;
    @MockitoBean
    private ContractController controller;

    private String jwtToken;
    final double initialGiroBalance = 1000.00;
    private DBContract myDBContract;
    private HttpHeaders header = new HttpHeaders();
    @BeforeEach
    public void setup() {
        final long anId = 1L;
        jwtToken = JWT.generate(anId);
        var dummyContrac = new DummyContract();
        String url = "/api/accounts/1/balance";
        myDBContract = new DBContract(dummyContrac.createDummyWithRE());

        when(contractRepository.findById(anId)).thenReturn(Optional.of(myDBContract));
        when(contractRepository.save(myDBContract)).thenReturn(myDBContract);
        when(controller.registerContract(any(), any(), any()))
                .thenReturn(new Result<>(Optional.of(new ContractRegistrationResult(anId, jwtToken)), Optional.empty()));

        header.set(HttpHeaders.AUTHORIZATION, jwtToken);
        header.set(HttpHeaders.CONTENT_TYPE, "application/json");
    }

    // Verwende BeforeEach, um nachfolgende Tests vorzubereiten
    // Schreibe Methoden, die die Endpunkte receive, send und transfer testen.
    // Verwende ParameterizedTest, um die verschiedenen Fälle zu testen
    // Lege data und util packages an, um die Daten und Hilfsklassen zu speichern.
    // Um ein lauffähigs Beispiel zu haben, ist in der BankingServer ein Schritt auskommentiert
    //        controller.registerContract(createDemoUser("demo", "demo"), 1000.0,
    //                response);
    // was müssen wir hinzufügen um diese Zeilen in der Hauptapplikation belassen zu können?

    @Test
    public void getAccountBalanceMock() {
        String url = "/api/accounts/1/balance";
        var header = new HttpHeaders();
        header.set(HttpHeaders.AUTHORIZATION, jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(header);

        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Type typeGetAccBalance = new TypeToken<Result<Double, Error>>() {
        }.getType();
        var balanceRespone = (Result<Double, Error>) Json.get().fromJson(response.getBody(), typeGetAccBalance);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        Assertions.assertEquals(1000.0, balanceRespone.value());
    }

    // receive Money
    @Test
    @DisplayName("Geld erhalten und Kontostand prüfen")
    public void happyPathReceiveMoney() {
        AccountHandler accountHandler = new AccountHandler();
        var amountReceived = 300.0;
        Id accountId = new Id(myDBContract.value().getId().parent(),1);
        String url = String.format(Locale.US,"/api/accounts/%d/receive?amount=%02.2f", accountId.child(), amountReceived);

        var header = new HttpHeaders();
        header.set(HttpHeaders.AUTHORIZATION, jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(header);
        myDBContract.value().getAccount(accountId).value().setBalance(initialGiroBalance + amountReceived);

        when(contractRepository.save(myDBContract)).thenReturn(myDBContract);
        var responseReceive = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println(responseReceive.getBody());
        Type typeGetAccReceive = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var receiveRespone = (Result<Boolean, Error>) Json.get().fromJson(responseReceive.getBody(), typeGetAccReceive);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), responseReceive.getStatusCode());
        Assertions.assertEquals(true, receiveRespone.value());

        // accountHandler.checkBalance(restTemplate, accountId, initialGiroBalance + amountReceived, jwtToken);
    }
    // send Money

    @Test
    public void happyPathSendMoney() {
        String iban = "DE02120300000000202051";
        Id accountId = new Id(myDBContract.value().getId().parent(),1);
        String url = String.format(Locale.US,"/api/accounts/%d/send", accountId.child());


        String body = Json.get().toJson(new SendMoney(new Iban(iban), 12));
        HttpEntity<String> entity = new HttpEntity<>(body, header);

        var responseReceive = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println(responseReceive.getBody());
        Type typeGetAccReceive = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var receiveRespone = (Result<Boolean, Error>) Json.get().fromJson(responseReceive.getBody(), typeGetAccReceive);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), responseReceive.getStatusCode());
        Assertions.assertEquals(true, receiveRespone.value());
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.0, -1.0, 55.475})
    public void failedSendMoney(double amount) {
        String iban = "DE02120300000000202051";
        Id accountId = new Id(myDBContract.value().getId().parent(),1);
        String url = String.format(Locale.US,"/api/accounts/%d/send", accountId.child());

        var header = new HttpHeaders();
        header.set(HttpHeaders.AUTHORIZATION, jwtToken);
        header.set(HttpHeaders.CONTENT_TYPE, "application/json");
        String body = Json.get().toJson(new SendMoney(new Iban(iban), amount));
        HttpEntity<String> entity = new HttpEntity<>(body, header);

        var responseReceive = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println(responseReceive.getBody());
        Type typeGetAccReceive = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var receiveRespone = (Result<Boolean, Error>) Json.get().fromJson(responseReceive.getBody(), typeGetAccReceive);
        Assertions.assertEquals(HttpStatusCode.valueOf(400), responseReceive.getStatusCode());
        Assertions.assertEquals("BadAmount", receiveRespone.error().error().toString());
    }



    // transfer Money

    @Test
    public void happyPathTransferMoney() {
        Id accountId = new Id(myDBContract.value().getId().parent(),1);
        String url = String.format(Locale.US,"/api/accounts/%d/transfer", accountId.child());

        var header = new HttpHeaders();
        header.set(HttpHeaders.AUTHORIZATION, jwtToken);
        header.set(HttpHeaders.CONTENT_TYPE, "application/json");
        String targetAccount = "00001:00002";
        TransferMoney transferMoney = new TransferMoney(new AccountId(targetAccount), 23);
        String body = Json.get().toJson(transferMoney);
        HttpEntity<String> entity = new HttpEntity<>(body, header);

        var responseReceive = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println(responseReceive.getBody());
        Type typeGetAccReceive = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var receiveRespone = (Result<Boolean, Error>) Json.get().fromJson(responseReceive.getBody(), typeGetAccReceive);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), responseReceive.getStatusCode());
        Assertions.assertEquals(true, receiveRespone.value());
    }

    @Test
    public void happyPathTransferMoney2() {
        AccountHandler accountHandler = new AccountHandler();
        Id accountId = new Id(myDBContract.value().getId().parent(),1);

        var wasSuccessFull = accountHandler.transferMoney(restTemplate, myDBContract, accountId, new Id(1,2), 23);
        Assertions.assertTrue(wasSuccessFull);
    }

    @Test
    public void failedTransferMoney() {
        String iban = "DE02120300000000202051";
        Id accountId = new Id(myDBContract.value().getId().parent(),1);
        String url = String.format(Locale.US,"/api/accounts/%d/transfer", accountId.child());

        var header = new HttpHeaders();
        header.set(HttpHeaders.AUTHORIZATION, jwtToken);
        header.set(HttpHeaders.CONTENT_TYPE, "application/json");
        String targetAccount = "00001:00003";
        TransferMoney transferMoney = new TransferMoney(new AccountId(targetAccount), 23);
        String body = Json.get().toJson(transferMoney);
        HttpEntity<String> entity = new HttpEntity<>(body, header);

        var responseReceive = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println(responseReceive.getBody());
        Type typeGetAccReceive = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var receiveRespone = (Result<Boolean, Error>) Json.get().fromJson(responseReceive.getBody(), typeGetAccReceive);
        Assertions.assertEquals(HttpStatusCode.valueOf(400), responseReceive.getStatusCode());
        Assertions.assertEquals("BadTarget", receiveRespone.error().error().toString());
    }

    @ParameterizedTest
    @ValueSource(doubles = {100, 10.15})
    public void receiveAmountMockOk(double amount) {
        String url = "/api/accounts/1/receive?amount=" + amount;
        HttpEntity<String> entity = new HttpEntity<>(header);

        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Type typeReceiveAmount = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var balanceRespone = (Result<Boolean, Error>) Json.get().fromJson(response.getBody(), typeReceiveAmount);

        Assertions.assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        Assertions.assertEquals(true, balanceRespone.value());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -100, 12.5643})
    public void receiveAmountMockFailed(double amount) {
        String url = "/api/accounts/1/receive?amount=" + amount;
        HttpEntity<String> entity = new HttpEntity<>(header);

        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Type typeReceiveAmount = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var balanceRespone = (Result<Boolean, Error>) Json.get().fromJson(response.getBody(), typeReceiveAmount);

        Assertions.assertEquals(HttpStatusCode.valueOf(400), response.getStatusCode());
        Assertions.assertEquals(Error.BadAmount, balanceRespone.error().error());
    }

    @Test
    public void sendAmountMockOk() {
        String url = "/api/accounts/1/send";

        String iban = org.iban4j.Iban.random().toString();

        var response = sendMoney(100, iban);

        Type typeReceiveAmount = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var balanceResponse = (Result<Boolean, Error>) Json.get().fromJson(response.getBody(), typeReceiveAmount);

        Assertions.assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        Assertions.assertEquals(true, balanceResponse.value());
    }

    @ParameterizedTest
    @CsvSource({
            "NL74INGB122, 100, BadIban",
            "NL74INGB1390722899, -100, BadAmount",
            "NL74INGB1390722899, 50000, BadBalance"
    })
    public void sendAmountMockFailed(String iban, double amount, Error expectedError) {

        var response = sendMoney(amount, iban);

        Type typeReceiveAmount = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var balanceResponse = (Result<Boolean, Error>) Json.get().fromJson(response.getBody(), typeReceiveAmount);

        Assertions.assertEquals(expectedError, balanceResponse.error().error());
    }

    private ResponseEntity<String> sendMoney(double amount, String target) {
        String url = "/api/accounts/1/send";

        var entity = new HttpEntity<>(Json.get().toJson(new SendMoney(new Iban(target), amount)), header);

        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

    }

}
