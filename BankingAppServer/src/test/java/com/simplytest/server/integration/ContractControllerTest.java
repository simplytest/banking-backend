package com.simplytest.server.integration;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.core.contracts.Contract;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.json.Json;
import com.simplytest.server.serviceObject.ContractControllerObject;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.lang.reflect.Type;
import java.sql.Date;
import java.util.Calendar;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ContractControllerObject contractControllerObject;

    String url = "/api/contracts";
    CustomerData customer;
    String jwt;
    HttpHeaders headers;

    private boolean createCustomerDTO() {
        var response = contractControllerObject.createDefaultCustomer();
        jwt = contractControllerObject.validateCustomerCreation(response);
        return true;
    }

    private void loginWithUser() {
        String endPointUrl = "/api/contracts/login/0001";
        String password = "demo";
        var loginResponse = restTemplate.postForEntity(endPointUrl, password, String.class);
        System.out.println(loginResponse);
        Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);
        Assertions.assertNotNull(response.get("result"));
        Assertions.assertTrue(response.get("result").isJsonPrimitive());
        jwt = response.get("result").getAsString();
    }

    private CustomerData createCustomer() {
        String customer = "{\n" + //
                "  \"firstName\": \"hanibal\",\n" + //
                "  \"password\": \"demo\",\n" + //
                "  \"lastName\": \"lecter\",\n" + //
                "  \"address\": {\n" + //
                "    \"country\": \"Germany\",\n" + //
                "    \"zipCode\": \"878765\",\n" + //
                "    \"street\": \"Mr Hyde Str\",\n" + //
                "    \"house\": \"42\",\n" + //
                "    \"city\": \"Gotham\",\n" + //
                "    \"email\": \"mey\"\n" + //
                "  },\n" + //
                "  \"type\": \"Private\",\n" + //
                "  \"birthDay\": \"1984-06-27T14:12:49.117Z\",\n" + //
                "  \"ustNumber\": \"35252342\",\n" + //
                "  \"companyName\": \"Imortal Inc.\"\n" + //
                "}";
        return Json.get().fromJson(customer, CustomerData.class);
    }

    @BeforeEach
    public void setup() {
        customer = createCustomer();
        loginWithUser();
        headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    // Contract registration, straightforward

    @Test
    public void simpleTestContractCreation() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        customer.password = "nixda";
        HttpEntity<String> requestEntity = new HttpEntity<>(Json.get().toJson(customer), headers);
        var response = restTemplate.postForEntity(url, requestEntity, String.class);
        System.out.println(response);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, Error>>() {
        }.getType();
        @SuppressWarnings("unchecked")
        var parsed = (Result<ContractRegistrationResult, Error>) Json.get().fromJson(response.getBody(), contractResponseType);

        System.out.println(parsed.value().id());
    }

    @Test
    public void simpleTestContractCreationUnderAge() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        customer.password = "nixda";
        System.out.println(Json.get().toJson(customer));
        var birthDay = Calendar.getInstance();
        birthDay.set(2020, 1, 1);

        customer.birthDay = birthDay.getTime();
        System.out.println(Json.get().toJson(customer));
        HttpEntity<String> requestEntity = new HttpEntity<>(Json.get().toJson(customer), headers);
        var response = restTemplate.postForEntity(url, requestEntity, String.class);
        System.out.println(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, Error>>() {
        }.getType();
        @SuppressWarnings("unchecked")
        var parsed = (Result<ContractRegistrationResult, Error>) Json.get().fromJson(response.getBody(), contractResponseType);

        System.out.println(parsed.error().error().name());
    }

    @Test
    public void getContractInformation() {
        Assertions.assertTrue(createCustomerDTO());
        headers.set(HttpHeaders.AUTHORIZATION, jwt);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> Assertions.assertTrue(response.getBody().contains("hanibal"))
        );
        Contract contract = Json.get().fromJson(response.getBody(), Contract.class);
        System.out.println(response.getBody());
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
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        System.out.println(response.getBody());
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> Assertions.assertTrue(response.getBody().contains("true"))
        );
    }
}
