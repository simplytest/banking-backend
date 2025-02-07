package com.simplytest.server.integration;

import com.simplytest.core.Id;
import com.simplytest.core.contracts.Contract;
import com.simplytest.server.api.ContractController;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerMockTest {

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
}
