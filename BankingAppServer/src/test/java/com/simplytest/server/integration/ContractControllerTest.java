package com.simplytest.server.integration;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.json.Json;
import com.simplytest.server.serviceObject.ContractControllerObject;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.lang.reflect.Type;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ContractControllerObject contractControllerObject;

    private boolean createCustomerDTO() {
        var response = contractControllerObject.createDefaultCustomer();
        contractControllerObject.validateCustomerCreation(response);
        return true;
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

    @Test
    public void happyPathUserLoginSuccessful() {
        String url = "/api/contracts/login/0001";
        String password = "demo";
        var loginResponse = restTemplate.postForEntity(url, password, String.class);
        System.out.println(loginResponse);
        Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);
        Assertions.assertNotNull(response.get("result"));
        Assertions.assertTrue(response.get("result").isJsonPrimitive());
        var myJWT = response.get("result").getAsString();
        Assertions.assertTrue(!myJWT.isEmpty());
        System.out.println(myJWT);
    }

    // Login with bad credentials
    @Test
    public void loginWithExistingCustomerBadCredentials() {
        String endPointUrl = "/api/contracts/login/0001";
        String password = "nix";
        var loginResponse = restTemplate.postForEntity(endPointUrl, password, String.class);
        System.out.println(loginResponse);
        JsonObject response = Json.get().fromJson(loginResponse.getBody(), JsonObject.class);
        // Gebündelte Assertions
        JsonObject error = response.getAsJsonObject("error");
        Assertions.assertAll(
                // Jede Assertion als Lambda
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST, loginResponse.getStatusCode()),
                () -> Assertions.assertNotNull(response.get("error")),
                () -> Assertions.assertEquals("BadCredentials", error.get("error").getAsString())
        );
    }

    // Login with non-existing contract ID 4444
    @Test
    public void loginTestFailedContractNotFound() {
        var url = "/api/contracts/login/4444";
        var pwd = "falsch";

        var response = restTemplate.postForEntity(url, pwd, String.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        System.out.println(response.getBody());
        JsonObject result = Json.get().fromJson(response.getBody(), JsonObject.class);
        Assertions.assertEquals("Not Found", result.get("error").getAsString());
    }

    // Contract registration, straightforward

    @Test
    public void simpleTestContractCreation() {
        String url = "/api/contracts";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        CustomerData customer = createCustomer();
        customer.password = "nixda";
        HttpEntity<String> requestEntity = new HttpEntity<>(Json.get().toJson(customer), headers);
        var response = restTemplate.postForEntity(url, requestEntity, String.class);
        System.out.println(response);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, Error>>() {
        }.getType();
        var parsed = (Result<ContractRegistrationResult, Error>) Json.get().fromJson(response.getBody(), contractResponseType);

        System.out.println(parsed.value().id());
    }







    @Test
    public void registerNewCustomer() {
        Assertions.assertTrue(createCustomerDTO());
    }
}
