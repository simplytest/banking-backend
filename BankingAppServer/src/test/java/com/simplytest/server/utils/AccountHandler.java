package com.simplytest.server.utils;

import com.google.common.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.server.apiData.AccountId;
import com.simplytest.server.apiData.TransferMoney;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

public class AccountHandler {

    public double checkBalance(TestRestTemplate restTemplate, Id accountId, double expectedBalance, String jwtToken) {
        String url = String.format("/api/accounts/%d/balance", accountId.child());
        var header = new HttpHeaders();
        header.set(HttpHeaders.AUTHORIZATION, jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(header);

        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println(response.getBody());
        Type typeGetAccBalance = new TypeToken<Result<Double, com.simplytest.core.Error>>() {
        }.getType();
        var balanceRespone = (Result<Double, Error>) Json.get().fromJson(response.getBody(), typeGetAccBalance);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        Assertions.assertEquals(expectedBalance, balanceRespone.value());
        return balanceRespone.value();
    }

    public boolean transferMoney(TestRestTemplate restTemplate, DBContract myDBContract, Id accountId ,Id realEastateAccount, double rueckzahlung) {

        String url = String.format("/api/accounts/%d/transfer", accountId.child());
        TransferMoney myMoney = new TransferMoney(
                new AccountId(String.format("%05d:%05d", myDBContract.value().getId().parent(), realEastateAccount.child())),
                rueckzahlung);
        var theContract = myDBContract.value();
        String jwtToken = JWT.generate(theContract.getId().parent());
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(Json.get().toJson(myMoney), headers);

        var responseTransfer = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println(responseTransfer.getBody());
        Type typeGetAccSend = new TypeToken<Result<Boolean, Error>>() {
        }.getType();
        var sendRespone = (Result<Boolean, Error>) Json.get().fromJson(responseTransfer.getBody(), typeGetAccSend);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), responseTransfer.getStatusCode());
        Assertions.assertEquals(true, sendRespone.value());
        return sendRespone.value();
    }
}
