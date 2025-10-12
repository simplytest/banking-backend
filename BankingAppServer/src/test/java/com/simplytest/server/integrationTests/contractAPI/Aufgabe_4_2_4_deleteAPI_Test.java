package com.simplytest.server.integrationTests.contractAPI;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.server.integrationTests.utils.ContractUtils;
import com.simplytest.server.json.Json;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Aufgabe_4_2_4_deleteAPI_Test {

    static final private String BASE_URL = "/api/contracts";

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void deleteContractTest() {
        String endPointUrl = BASE_URL;
        var result = ContractUtils.registerNewCustomer(restTemplate, "SpringTest", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", result.jwtToken());

        var contractDeleteResponse = restTemplate.exchange(endPointUrl, HttpMethod.DELETE,  new HttpEntity<>(headers),  String.class);
        Assertions.assertEquals(HttpStatus.OK, contractDeleteResponse.getStatusCode());
        System.out.println(contractDeleteResponse.getBody());

        final TypeToken<Result<Boolean, Error>> deleteResponseType = new TypeToken<>() {};
        var response = Json.get().fromJson(contractDeleteResponse.getBody(), deleteResponseType);

        Assertions.assertTrue(response.successful());
        Assertions.assertEquals("true", response.value().toString());

        // prüfen, dass der gelöschte Vertrag nicht mehr abgerufen werden kann
        var contractInfoResponse = restTemplate.exchange(endPointUrl, HttpMethod.GET,  new HttpEntity<>(headers),  String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, contractInfoResponse.getStatusCode());

    }



}
