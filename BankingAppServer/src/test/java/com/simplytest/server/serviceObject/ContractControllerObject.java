package com.simplytest.server.serviceObject;

import com.google.gson.reflect.TypeToken;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.data.DummyCustomer;
import com.simplytest.server.json.Json;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

@Service
public class ContractControllerObject {

    @Autowired
    private TestRestTemplate restTemplate;
    private String contractUrl = "/api/contracts";


    public ContractControllerObject(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> createDefaultCustomer() {
        System.out.println("Creating default customer");
        var dummyCustomer = new DummyCustomer();
        var customer = dummyCustomer.createDefaultCustomerDTO();
        ResponseEntity<String> response = restTemplate.postForEntity(contractUrl, customer, String.class);
        return response;
    }
    public String validateCustomerCreation(ResponseEntity<String> response) {
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, Error>>() {
        }.getType();
        @SuppressWarnings("unchecked")
        final var parsed = (Result<ContractRegistrationResult, Error>) Json.get().fromJson(response.getBody(),
                contractResponseType);
        System.out.println(parsed.value().id());
        System.out.println(parsed.value().JWT());
        String jwtToken = parsed.value().JWT();
        return jwtToken;
    }
}
