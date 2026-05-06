package com.simplytest.server.intefrationTests.contractAPI;

import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplytest.core.customers.Address;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.apiData.CustomerData.CustomerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Aufgabe_4_2_3_contractGetDeleteAPI_Test {

    final private String BASE_URL = "/api/contracts";

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String jwt;
    private long contractId;


    private CustomerData createCustomer(String name, String password) {
        var address = new Address();
        address.setCountry("Germany");
        address.setZipCode("12345");
        address.setStreet("New Street");
        address.setHouse("1");
        address.setCity("My City");
        address.setEmail("mail@test.de");

        var cal = Calendar.getInstance();
        cal.set(2000, Calendar.JANUARY, 1);
        Date birthDay = cal.getTime();

        var customer = new CustomerData();
        customer.firstName = name;
        customer.lastName = name;
        customer.password = password;
        customer.address = address;
        customer.type = CustomerType.Private;
        customer.birthDay = birthDay;

        return customer;
    }

    @BeforeEach
    public void registerNewCustomer() throws Exception {
        String endPointUrl = BASE_URL + "?initialBalance=500";

        var newCustomer = createCustomer("TestUser", "password");

        var registerResponse = restTemplate.postForEntity(endPointUrl, newCustomer, String.class);
        Assertions.assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        JsonNode response = objectMapper.readTree(registerResponse.getBody());
        jwt = response.path("result").path("JWT").asText();
        contractId = response.path("result").path("id").asLong();

        Assertions.assertFalse(jwt.isEmpty(), "JWT should not be empty after registration");
        Assertions.assertTrue(contractId > 0, "Contract ID should be > 0 after registration");

        System.out.println("BeforeEach: registered contract " + contractId);
    }


    @Test
    public void getContractReturnsCustomerData() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, jwt);

        var getResponse = restTemplate.exchange(BASE_URL, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        System.out.println(getResponse);

        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        JsonNode contract = objectMapper.readTree(getResponse.getBody());

        String lastName = contract.path("customer").path("lastName").asText();
        Assertions.assertEquals("TestUser", lastName);
        System.out.println("GET contract lastName: " + lastName);
    }

    @Test
    public void getContractWithInvalidTokenReturnsForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "invalid.token.value");

        var getResponse = restTemplate.exchange(BASE_URL, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        System.out.println(getResponse);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, getResponse.getStatusCode());
    }

    @Test
    public void deleteContractSucceeds() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, jwt);

        var deleteResponse = restTemplate.exchange(BASE_URL, HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class);

        System.out.println(deleteResponse);

        Assertions.assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        JsonNode response = objectMapper.readTree(deleteResponse.getBody());
        boolean successful = response.path("result").asBoolean();
        Assertions.assertTrue(successful);
        System.out.println("DELETE contract result: " + successful);
    }

    @Test
    public void deleteContractWithInvalidTokenReturnsForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "invalid.token.value");

        var deleteResponse = restTemplate.exchange(BASE_URL, HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class);

        System.out.println(deleteResponse);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, deleteResponse.getStatusCode());
    }
}
