package com.simplytest.server.integration;

import com.simplytest.core.Id;
import com.simplytest.server.apiData.Iban;
import com.simplytest.server.apiData.SendMoney;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerMockWebServerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    ContractRepository repository;

    private HttpHeaders headers;
    private MockWebServer mockExternalService;
    private DBContract myDBContract;
    private String endpointUrl;
    private Id accountId;

    @BeforeEach
    public void setUp() throws IOException {
        mockExternalService = new MockWebServer();
        mockExternalService.start(8081);
        var dummyContract = new DummyContract();
        myDBContract = repository.save(new DBContract(dummyContract.createDummyContract()));
        String jwtToken = JWT.generate(myDBContract.id());
        headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        accountId = new Id(myDBContract.value().getId().parent(),1);
        endpointUrl = String.format("/api/accounts/%d/sendexternal", accountId.child());
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockExternalService.shutdown();
    }
    @Test
    public void happyPathSendExternal() {
        mockExternalService.enqueue(new MockResponse().setResponseCode(200));
        String iban = "DE89370400440532013000";
        String body = Json.get().toJson(new SendMoney(new Iban(iban), 12));

        System.out.println("Body: " + body);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        var response = restTemplate.exchange(endpointUrl, HttpMethod.POST, entity, String.class);
        System.out.println("Response: " + response.getBody());
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> Assertions.assertTrue(response.getBody().contains("true"))
        );
    }
    @Test
    public void failedSendExternal() {
        mockExternalService.enqueue(new MockResponse().setResponseCode(400));
        String iban = "DE89370400440532013000";
        String body = Json.get().toJson(new SendMoney(new Iban(iban), 12));

        System.out.println("Body: " + body);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        var response = restTemplate.exchange(endpointUrl, HttpMethod.POST, entity, String.class);
        System.out.println("Response: " + response.getBody());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @ParameterizedTest(name = "IBAN {0} responses with {1}")
    @MethodSource("data")
    public void transferMoneyExternMock(String iban, HttpStatus expectedStatus) {
        // arrange
        Id accountId = new Id(myDBContract.value().getId().parent(),1);
        String url = String.format(Locale.US,"/api/accounts/%d/send", accountId.child());
        //Iban ibanInst = org.iban4j.Iban.valueOf(iban);
        mockExternalService.enqueue(new MockResponse().setResponseCode(expectedStatus.value()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = Json.get().toJson(new SendMoney(new Iban(iban), 12));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        // act
        var response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        // assert
        Assertions.assertEquals(expectedStatus, response.getStatusCode());
    }

    static Stream<Arguments> data() {
        return Stream.of(
                Arguments.arguments("DE45120300000000202053", HttpStatus.OK),
                Arguments.arguments("DE18120300000000202054", HttpStatus.OK),
                Arguments.arguments("DE60120300000000202074", HttpStatus.OK),
                Arguments.arguments("DE60120300000000202052", HttpStatus.BAD_REQUEST),
                Arguments.arguments("DE60120300000000202051", HttpStatus.BAD_REQUEST),
                Arguments.arguments("DE60120300000000202", HttpStatus.BAD_REQUEST)
        );
    }

}
