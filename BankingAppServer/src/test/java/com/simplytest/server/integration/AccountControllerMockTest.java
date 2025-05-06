package com.simplytest.server.integration;

import com.google.common.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Type;
import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class AccountControllerMockTest {
    @MockitoBean
    private ContractRepository repository;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void getAccountBalanceMock() {
        String url = "/api/accounts/1/balance";
        var dummyContrac = new DummyContract();
        var myDBContract = new DBContract(dummyContrac.createDummyWithRE());


        final long anId = 1L;
        String jwtToken = JWT.generate(anId);

        when(repository.findById(anId)).thenReturn(Optional.of(myDBContract));

        var header = new HttpHeaders();
        header.set(HttpHeaders.AUTHORIZATION, jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(header);

        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Type typeGetAccBalance = new TypeToken<Result<Double, com.simplytest.core.Error>>() {
        }.getType();
        var balanceRespone = (Result<Double, Error>) Json.get().fromJson(response.getBody(), typeGetAccBalance);
    }
}
