package com.simplytest.server.integration;

import com.google.common.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.core.contracts.Contract;
import com.simplytest.server.api.ContractController;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.AccountHandler;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerMockTest {

    final double initialGiroBalance = 1000.00;
    @MockBean
    private ContractRepository repository;
    @MockBean
    private ContractController controller;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    @DisplayName("Den Kontostand abfragen")
    public void happyPathCheckBalance() {
        final long anId = 1L;
        DummyContract dummyContract = new DummyContract();
        Contract myContract = dummyContract.createDummyContract();
        final DBContract dbContract = new DBContract(myContract);
        String jwtToken = JWT.generate(myContract.getId());
        when(repository.findById(myContract.getId().parent()))
                .thenReturn(Optional.of(new DBContract(myContract)));
        when(repository.save(dbContract)).thenReturn(dbContract);
        when(controller.registerContract(any(), any(), any()))
                .thenReturn(new Result<>(Optional.of(new ContractRegistrationResult(anId, jwtToken)), Optional.empty()));

        String url = String.format("/api/accounts/%d/balance", myContract.getId().parent());
        // create header for HTTP Request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // send HTTP Request
        var result = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println(result.getBody());
    }

    @Test
    @DisplayName("Geld erhalten und Kontostand prüfen")
    public void happyPathReceiveMoney() {
        AccountHandler accountHandler = new AccountHandler();
        final long anId = 1L;
        DummyContract dummyContract = new DummyContract();
        Contract myContract = dummyContract.createDummyContract();
        final DBContract dbContract = new DBContract(myContract);
        String jwtToken = JWT.generate(myContract.getId().parent());
        when(repository.findById(myContract.getId().parent()))
                .thenReturn(Optional.of(new DBContract(myContract)));
        when(repository.save(dbContract)).thenReturn(dbContract);
        when(controller.registerContract(any(), any(), any()))
                .thenReturn(new Result<>(Optional.of(new ContractRegistrationResult(anId, jwtToken)), Optional.empty()));
        var amountReceived = 300.0;
        Id accountId = new Id(myContract.getId().parent(),1);
        String url = String.format(Locale.US,"/api/accounts/%d/receive?amount=%02.2f", accountId.child(), amountReceived);


        var header = new HttpHeaders();
        header.set(HttpHeaders.AUTHORIZATION, jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(header);

        myContract.getAccount(accountId).value().setBalance(initialGiroBalance + amountReceived);
        when(repository.findById(myContract.getId().parent()))
                .thenReturn(Optional.of(new DBContract(myContract)));
        var responseReceive = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println(responseReceive.getBody());
        Type typeGetAccReceive = new TypeToken<Result<Boolean, com.simplytest.core.Error>>() {
        }.getType();
        var receiveRespone = (Result<Boolean, Error>) Json.get().fromJson(responseReceive.getBody(), typeGetAccReceive);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), responseReceive.getStatusCode());
        Assertions.assertEquals(true, receiveRespone.value());

        accountHandler.checkBalance(restTemplate, accountId, initialGiroBalance + amountReceived, jwtToken);
    }
}
