package com.simplytest.server.integration;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.simplytest.core.Error;
import com.simplytest.core.contracts.Contract;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.json.Json;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.lang.reflect.Type;
import java.sql.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerTestU2 {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void loginWithExistingUser() {
        String endPointUrl = "/api/contracts/login/0001";
        String password = "demo";
        var loginResponse = restTemplate.postForEntity("/api/contracts/login/0001", password, String.class);
        System.out.println(loginResponse);
        Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);
        Assertions.assertTrue(response.isJsonObject());
        Assertions.assertNotNull(response.get("result"));
        var myJWT = response.get("result").getAsString();
        System.out.println(myJWT);
    }

    @Test
    public void loginWithExistingCustomerBadCredentials() {
        String endPointUrl = "/api/contracts/login/0001";
        String password = "nix";
        var loginResponse = restTemplate.postForEntity(endPointUrl, password, String.class);
        System.out.println(loginResponse);
        //Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        // prüfe, dass im Ergebnis ein JWT Token enthalten ist
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);
        // Gebündelte Assertions

        Assertions.assertAll(
                // Jede Assertion als Lambda
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST, loginResponse.getStatusCode()),
                () -> Assertions.assertNotNull(response.get("error"))
        );
    }

    @Test
    public void testContractRegistration() {
        String url = "/api/contracts";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonBody = "{\n" + //
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
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        var createResponse = restTemplate.postForEntity(url, requestEntity, String.class);
        Assertions.assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        JsonObject response = Json.get().fromJson(createResponse.getBody(), JsonObject.class);
        JsonObject results = response.getAsJsonObject("result");
        Assertions.assertNotNull(results.get("id"));
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, Error>>() {
        }.getType();
        final var parsed = (Result<ContractRegistrationResult, Error>) Json.get().fromJson(createResponse.getBody(),
                contractResponseType);
        Assertions.assertNotNull(parsed.value().JWT());
        System.out.println(createResponse.getBody());
    }

    @Test
    public void testContractRegistrationDTO() {
        CustomerData customer = createCustomer();
        customer.password = "demo";
        var createResponse = restTemplate.postForEntity("/api/contracts", customer, String.class, -1000.21);
        Assertions.assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        JsonObject response = Json.get().fromJson(createResponse.getBody(), JsonObject.class);
        JsonObject results = response.getAsJsonObject("result");
        Assertions.assertNotNull(results.get("id"));
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, com.simplytest.core.Error>>() {
        }.getType();
        final var parsed = (Result<ContractRegistrationResult, Error>) Json.get().fromJson(createResponse.getBody(),
                contractResponseType);
        Assertions.assertNotNull(parsed.value().JWT());
        System.out.println(createResponse.getBody());
    }

    @Test
    public void testContractRegistrationDTOMAbgelehnt() {
        CustomerData customer = createCustomer();
        customer.birthDay = Date.valueOf("2020-06-27");
        var createResponse = restTemplate.postForEntity("/api/contracts", customer, String.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, createResponse.getStatusCode());
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, Error>>() {
        }.getType();
        System.out.println(createResponse.getBody());
        final var parsed = (Result<ContractRegistrationResult, Error>) Json.get().fromJson(createResponse.getBody(),
                contractResponseType);
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST, createResponse.getStatusCode()),
                () -> Assertions.assertNotNull(parsed.error()),
                () -> Assertions.assertTrue(parsed.error().error().toString().contains("Underage"))

        );
        System.out.println(parsed.error());
    }

    // Übung 2-3, Vertrag eines Kunden abrufen
    @Test
    public void getContractInformation() {
        String url = "/api/contracts";
        var customerContract = registerNewCustomer();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, customerContract.value().JWT());
        headers.setContentType(MediaType.APPLICATION_JSON);

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

    // Übung 2-4 Vertrag eines Kunden löschen
    @Test
    public void deleteCustomerContract() {
        String url = "/api/contracts";
        var customerContract = registerNewCustomer();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, customerContract.value().JWT());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        System.out.println(response.getBody());
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> Assertions.assertTrue(response.getBody().contains("true"))
        );
        var customerId = customerContract.value().id();
        String endPointUrl = "/api/contracts/login/" + customerId;
        String password = "demo";
        var loginResponse = restTemplate.postForEntity(endPointUrl, password, String.class);
        System.out.println(loginResponse);

        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.NOT_FOUND, loginResponse.getStatusCode()),
                () -> Assertions.assertTrue(response.getBody().contains("404 NOT_FOUND"))
        );
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

    private Result<ContractRegistrationResult, Error> registerNewCustomer() {
        CustomerData customer = createCustomer();
        var createResponse = restTemplate.postForEntity("/api/contracts", customer, String.class, -1000.21);
        Assertions.assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        JsonObject response = Json.get().fromJson(createResponse.getBody(), JsonObject.class);
        JsonObject results = response.getAsJsonObject("result");
        Assertions.assertNotNull(results.get("id"));
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, com.simplytest.core.Error>>() {
        }.getType();
        return Json.get().fromJson(createResponse.getBody(),
                contractResponseType);
    }
}
