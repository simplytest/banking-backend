package com.simplytest.server.integration;

/*
<dependency>
  <groupId>com.squareup.okhttp3</groupId>
  <artifactId>mockwebserver</artifactId>
  <version>4.12.0</version>
  <scope>test</scope>
</dependency>
*/

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
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

    @BeforeEach
    public void setup() throws IOException {
        mockExternalService = new MockWebServer();
        mockExternalService.start(8081);
        var dummyContract = new DummyContract();
        myDBContract = repository.save(new DBContract(dummyContract.createDummyContract()));
        String jwtToken = JWT.generate(myDBContract.id());
        headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockExternalService.shutdown();
    }

    @Test
    public void happyPathSendExteranl() {
        Id accountId = new Id(myDBContract.value().getId().parent(),1);
        String url = String.format("/api/accounts/%d/sendexternal", accountId.child());

        mockExternalService.enqueue(new MockResponse().setResponseCode(200));
        String iban = "DE89370400440532013000";
        String body = Json.get().toJson(new SendMoney(new Iban(iban), 12));

        System.out.println("Body: " + body);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        var response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println("Response: " + response.getBody());
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> Assertions.assertTrue(response.getBody().contains("true"))
        );
    }


}
