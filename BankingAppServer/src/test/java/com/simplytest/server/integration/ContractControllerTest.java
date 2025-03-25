package com.simplytest.server.integration;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.core.customers.Address;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.json.Json;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Type;
import java.util.Calendar;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;


    String jwtToken = "your.jwt.token.here";
    // Variante Testdata Anlage über DTO
    private CustomerData createDummyCustomerDTO() {
        var customer = new CustomerData();
        customer.type = CustomerData.CustomerType.Private;

        var birthDay = Calendar.getInstance();
        birthDay.set(2000, 01, 01);

        customer.birthDay = birthDay.getTime();
        customer.address = new Address();
        customer.firstName = "Foo";
        customer.lastName = "Bar";

        customer.password = "password";

        customer.address.setStreet("Some Street");
        customer.address.setZipCode("12345");

        return customer;
    }

    private boolean createCustomerDTO() {
        var customer = createDummyCustomerDTO();
        ResponseEntity<String> response = restTemplate.postForEntity("/api/contracts", customer, String.class);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, java.lang.Error>>() {
        }.getType();
        @SuppressWarnings("unchecked")
        final var parsed = (Result<ContractRegistrationResult, java.lang.Error>) Json.get().fromJson(response.getBody(),
                contractResponseType);
        System.out.println(parsed.value().id());
        System.out.println(parsed.value().JWT());
        jwtToken = parsed.value().JWT();
        return true;
    }

    @Test
    public void registerNewCustomer() {
        Assertions.assertTrue(createCustomerDTO());
    }

    @Test
    public void registerAnotherCustomer() {
        Assertions.assertTrue(createCustomerDTO());
    }

    @Test
    public void registerYetAnotherCustomer() {
        Assertions.assertTrue(createCustomerDTO());
    }

    @Test
    public void loginTestWithValidData() {
        String url = "/api/contracts/login/0001";
        String password = "demo";
        var loginResponse = restTemplate.postForEntity(url, password, String.class);
        System.out.println(loginResponse);
        Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        // prüfe, dass im Ergebnis ein JWT Token enthalten ist
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);
        Assertions.assertNotNull(response.get("result"));
        Assertions.assertTrue(response.get("result").isJsonPrimitive());
        var myJWT = response.get("result").getAsString();
        Assertions.assertTrue(!myJWT.isEmpty());
        System.out.println(myJWT);
    }

    // Validieren des Logins
    // mit gültigen Daten, was können wir auswerten?
    // Login mit falschen Daten, welche Fehlermeldung wird zurückgegeben?
    // Login mit einem nicht existierenden Kunden, was passiert?

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
    public void loginTestInvalidPasswordFailed() {
        var url = "/api/contracts/login/1";
        var pwd = "falsch";

        var response = restTemplate.postForEntity(url, pwd, String.class);


        var result = Json.get().fromJson(response.getBody(), Result.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertTrue(Error.BadCredentials.toString().equals(result.error().error().toString()));

        System.out.println(response.getBody());
    }

    @Test
    public void loginTestInvalidCredentialsFailed() {
        var url = "/api/contracts/login/1234";
        var pwd = "falsch";

        var response = restTemplate.postForEntity(url, pwd, String.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        System.out.println(response.getBody());
    }

}
