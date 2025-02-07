package com.simplytest.server.integration;

import com.google.common.reflect.TypeToken;
import com.simplytest.core.Id;
import com.simplytest.core.contracts.Contract;
import com.simplytest.server.apiData.AccountId;
import com.simplytest.server.apiData.Iban;
import com.simplytest.server.apiData.SendMoney;
import com.simplytest.server.apiData.TransferMoney;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import com.simplytest.core.Error;

import java.lang.reflect.Type;
import java.util.Locale;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerTest {
    final double initialGiroBalance = 1000.00;
    DBContract myDBContract;
    Contract theContract;
    AccountHandler accountHandler;
    @Autowired
    private ContractRepository repository;

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeEach
    public void createCustomerInDatabase() {
        accountHandler = new AccountHandler();
        DummyContract prepContract = new DummyContract();
        myDBContract = repository.save(new DBContract(prepContract.createDummyContract()));
        theContract = myDBContract.value();
        theContract.getAccounts().forEach((id, account) -> {
            System.out.println("Account: " + account);
        });
        Id accountId = new Id(1,1);
        System.out.println(theContract.getAccount(accountId).value().toString());

    }

    @Test
    @DisplayName("Den Kontostand abfragen")
    public void happyPathCheckBalance() {
        Id accountId = new Id(theContract.getId().parent(),1);
        String jwtToken = JWT.generate(theContract.getId().parent());
        double balance = accountHandler.checkBalance(restTemplate, accountId, initialGiroBalance, jwtToken);
        Assertions.assertEquals(initialGiroBalance, balance);
    }
    // receive Money

    @Test
    @DisplayName("Geld erhalten und Kontostand prüfen")
    public void happyPathReceiveMoney() {
        var amountReceived = 300.0;
        Id accountId = new Id(theContract.getId().parent(),1);
        String url = String.format(Locale.US,"/api/accounts/%d/receive?amount=%02.2f", accountId.child(), amountReceived);

        String jwtToken = JWT.generate(theContract.getId().parent());
        var header = new HttpHeaders();
        header.set(HttpHeaders.AUTHORIZATION, jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(header);

        var responseReceive = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println(responseReceive.getBody());
        Type typeGetAccReceive = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var receiveRespone = (Result<Boolean, Error>) Json.get().fromJson(responseReceive.getBody(), typeGetAccReceive);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), responseReceive.getStatusCode());
        Assertions.assertEquals(true, receiveRespone.value());

        accountHandler.checkBalance(restTemplate, accountId, amountReceived + initialGiroBalance, jwtToken);
    }

    // send Money

    @Test
    @DisplayName("Geld senden und Kontostand prüfen")
    public void happyPathSendMoney() {
        double amountSent = 12.0;
        double balance;
        String iban = "DE02120300000000202051";
        Id accountId = new Id(theContract.getId().parent(),1);
        String url = String.format("/api/accounts/%d/send", accountId.child());

        balance = accountHandler.checkBalance(restTemplate, accountId, initialGiroBalance, JWT.generate(theContract.getId().parent()));
        String jwtToken = JWT.generate(theContract.getId().parent());
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = Json.get().toJson(new SendMoney(new Iban(iban), amountSent));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        var responseSend = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println(responseSend.getBody());
        Type typeGetAccSend = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var sendRespone = (Result<Boolean, Error>) Json.get().fromJson(responseSend.getBody(), typeGetAccSend);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), responseSend.getStatusCode());
        Assertions.assertEquals(true, sendRespone.value());

        accountHandler.checkBalance(restTemplate, accountId, balance - amountSent, jwtToken);
    }

    // transfer money
    @ParameterizedTest
    @ValueSource(doubles = { 350.0, 1000.0})
    public void happyPathTransferMoneyCheck(double rueckzahlung) {
        final double initialRealEastateBalance = -35000.00;
        Id accountId = new Id(theContract.getId().parent(),1);
        Id realEastateAccount = new Id(theContract.getId().parent(),2);
        String url = String.format("/api/accounts/%d/transfer", accountId.child());
        var dummyContract = new DummyContract();
        theContract = dummyContract.addRealEstateToDummy(theContract, -initialRealEastateBalance, 500);
        theContract = dummyContract.changeCustomerData(theContract, "Hanibal", "Lecter");
        myDBContract = repository.save(new DBContract(theContract));

        accountHandler.checkBalance(restTemplate, new Id(theContract.getId().parent(),1), initialGiroBalance, JWT.generate(theContract.getId().parent()));
        accountHandler.checkBalance(restTemplate, new Id(theContract.getId().parent(),2), initialRealEastateBalance, JWT.generate(theContract.getId().parent()));

        accountHandler.transferMoney(restTemplate, myDBContract, accountId, realEastateAccount, rueckzahlung);

        accountHandler.checkBalance(restTemplate, new Id(theContract.getId().parent(),1), initialGiroBalance - rueckzahlung, JWT.generate(theContract.getId().parent()));
        accountHandler.checkBalance(restTemplate, new Id(theContract.getId().parent(),2), initialRealEastateBalance + rueckzahlung, JWT.generate(theContract.getId().parent()));

    }


}
