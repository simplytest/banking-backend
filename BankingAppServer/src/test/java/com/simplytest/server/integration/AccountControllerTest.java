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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import com.simplytest.core.Error;

import java.lang.reflect.Type;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerTest {
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
    public void happyPathCheckBalance2() {
        Id accountId = new Id(theContract.getId().parent(),1);

        String jwtToken = JWT.generate(theContract.getId().parent());
        double balance = accountHandler.checkBalance(restTemplate, accountId, 0.0, jwtToken);
        Assertions.assertEquals(0.0, balance);
    }
    // receive Money

    @Test
    @DisplayName("Geld erhalten und Kontostand prüfen")
    public void happyPathReceiveMoney() {
        Id accountId = new Id(theContract.getId().parent(),1);
        String url = String.format("/api/accounts/%d/receive?amount=%d", accountId.child(), 300);

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

        accountHandler.checkBalance(restTemplate, accountId, 300.0, jwtToken);
    }

    // send Money

    @Test
    @DisplayName("Geld senden und Kontostand prüfen")
    public void happyPathSendMoney() {
        double amountSent = 12.0;
        double balance = 1000.0;
        String iban = "DE02120300000000202051";
        Id accountId = new Id(theContract.getId().parent(),1);
        String url = String.format("/api/accounts/%d/send", accountId.child());

        balance = accountHandler.checkBalance(restTemplate, accountId, balance, JWT.generate(theContract.getId().parent()));
        String jwtToken = JWT.generate(theContract.getId().parent());
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = Json.get().toJson(new SendMoney(new Iban(iban), 12));
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


//    TransferMoney myMoney = new TransferMoney(
//            new AccountId(String.format("%05d:%05d", myContract.value().getId().parent(), frAccountId)),
//            sparBetrag);
//
//    entity = new HttpEntity<>(Json.get().toJson(myMoney), header);
}
