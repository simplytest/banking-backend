package com.simplytest.server.intefrationTests.contractAPI;

import java.time.Year;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplytest.core.customers.Address;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.apiData.CustomerData.CustomerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Aufgabe_4_2_2_registerAPI_DTO_Test {

    final private String BASE_URL = "/api/contracts";

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();


    private CustomerData createCustomer(String name, String password, Date birthDay) {
        var address = new Address();
        address.setCountry("Germany");
        address.setZipCode("12345");
        address.setStreet("New Street");
        address.setHouse("1");
        address.setCity("My City");
        address.setEmail("mail@test.de");

        var customer = new CustomerData();
        customer.firstName = name;
        customer.lastName = name;
        customer.password = password;
        customer.address = address;
        customer.type = CustomerType.Private;
        customer.birthDay = birthDay;

        return customer;
    }

    private Date adultBirthDay() {
        var cal = Calendar.getInstance();
        cal.set(2000, Calendar.JANUARY, 1);
        return cal.getTime();
    }

    private Date underAgeBirthDay() {
        var cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Year.now().getValue());
        return cal.getTime();
    }


    @Test
    public void registerTestWithValidData() throws Exception {
        String endPointUrl = BASE_URL + "?initialBalance=500";

        var newCustomer = createCustomer("SpringTest", "password", adultBirthDay());

        var registerResponse = restTemplate.postForEntity(endPointUrl, newCustomer, String.class);
        System.out.println(registerResponse);

        Assertions.assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        JsonNode response = objectMapper.readTree(registerResponse.getBody());

        String jwt = response.path("result").path("JWT").asText();
        Assertions.assertFalse(jwt.isEmpty());
        System.out.println("JWT:" + jwt);

        int contractId = response.path("result").path("id").asInt();
        Assertions.assertTrue(contractId > 0);
        System.out.println("Contract ID: " + contractId);
    }


    @Test
    public void registerTestWithUnderAgeCustomer() throws Exception {
        String endPointUrl = BASE_URL + "?initialBalance=500";

        var newCustomer = createCustomer("SpringTest", "password", underAgeBirthDay());

        var registerResponse = restTemplate.postForEntity(endPointUrl, newCustomer, String.class);
        System.out.println(registerResponse);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, registerResponse.getStatusCode());

        JsonNode response = objectMapper.readTree(registerResponse.getBody());

        String errorText = response.path("error").path("error").asText();
        Assertions.assertFalse(errorText.isEmpty());
        System.out.println("Expected error:" + errorText);

        Assertions.assertTrue(errorText.contains("Underage"));
    }
}
