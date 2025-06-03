package com.simplytest.server.integration;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.contracts.Contract;
import com.simplytest.server.api.ContractController;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.data.DummyCustomer;
import com.simplytest.server.json.Json;
import com.simplytest.server.serviceObjects.ContractControllerObject;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerRegistrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    CustomerData customerData;
    String endpointUrl = "/api/contracts";
    String jwtToken;
    ContractControllerObject contractObject;
    HttpHeaders headers;

    @BeforeEach
    public void setUp() {
        contractObject = new ContractControllerObject(restTemplate);
        DummyCustomer dummyCustomer = new DummyCustomer();
        customerData = dummyCustomer.createDefaultCustomerDTO();

        headers = new HttpHeaders();
        //headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    public void happyPathCreateCustomer() {
        ResponseEntity<String> response = contractObject.createDefaultCustomer();
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, Error>>() {
        }.getType();
        jwtToken = contractObject.validateCustomerCreation(response);
        Assertions.assertNotNull(jwtToken);
    }

    @Test
    public void underAgeCreateCustomer() {
        var birthDay = Calendar.getInstance();
        birthDay.set(2020, 1, 1);
        customerData.birthDay = birthDay.getTime();
        ResponseEntity<String> response = contractObject.createCustomer(customerData);
        System.out.println(response.getBody());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, com.simplytest.core.Error>>() {
        }.getType();
        @SuppressWarnings("unchecked")
        final var parsed = (Result<ContractRegistrationResult, com.simplytest.core.Error>) Json.get().fromJson(response.getBody(),
                contractResponseType);
        System.out.println(parsed.error().error().name());
        Assertions.assertEquals("Underage", parsed.error().error().name());
    }

    @Test
    public void happyPathGetContract() {
        ResponseEntity<String> response = contractObject.createDefaultCustomer();
        jwtToken = contractObject.validateCustomerCreation(response);
        headers.set(HttpHeaders.AUTHORIZATION, jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> getResponse = restTemplate.exchange(endpointUrl, HttpMethod.GET, entity, String.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode()),
                () -> Assertions.assertTrue(getResponse.getBody().contains("hanibal"))
        );
        Contract contract = Json.get().fromJson(getResponse.getBody(), Contract.class);
        System.out.println(getResponse.getBody());
        Assertions.assertAll(
                () -> Assertions.assertEquals("hanibal", contract.getCustomer().getFirstName()),
                () -> Assertions.assertEquals("lecter", contract.getCustomer().getLastName()),
                () -> Assertions.assertEquals("Mr Hyde Str", contract.getCustomer().getAddress().getStreet()),
                () -> Assertions.assertEquals("Gotham", contract.getCustomer().getAddress().getCity()),
                () -> Assertions.assertEquals("Germany", contract.getCustomer().getAddress().getCountry()),
                () ->Assertions.assertEquals("878765", contract.getCustomer().getAddress().getZipCode()),
                () ->Assertions.assertEquals("42", contract.getCustomer().getAddress().getHouse()),
                () ->Assertions.assertEquals("mey", contract.getCustomer().getAddress().getEmail())

        );

    }

    @Test
    public void deleteContract() {
        ResponseEntity<String> createResponse = contractObject.createDefaultCustomer();
        jwtToken = contractObject.validateCustomerCreation(createResponse);
        headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(endpointUrl, HttpMethod.DELETE, entity, String.class);
        System.out.println(response.getBody());
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> Assertions.assertTrue(response.getBody().contains("true"))
        );
    }
}
