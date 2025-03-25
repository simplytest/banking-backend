package com.simplytest.server.integration;

import com.google.common.reflect.TypeToken;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import org.iban4j.Iban;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.Optional;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.*;

@EnableWireMock(@ConfigureWireMock(portProperties = "localhost", port = 8081))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"validationurl = http://localhost:8081"})
public class AccountControllerMockTest2 {
    @Autowired
    TestRestTemplate restTemplate;
    @MockitoBean
    ContractRepository contractRepository;
    private HttpHeaders headers;

    @BeforeEach
    public void setup() {
        var dummyContract = new DummyContract();
        var dbContract = new DBContract(dummyContract.createDummyContract());
        when(contractRepository.findById(dbContract.id())).thenReturn(Optional
                .of(dbContract));
        String jwtToken = JWT.generate(dbContract.id());
        headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, jwtToken);
    }

    @Test
    public void getAccountBalanceMock() {
        // arrange
        String url = "/api/accounts/1/balance";
        HttpEntity<String> entity = new HttpEntity<>(headers);
        // act
        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Type typeGetAccBalance = new TypeToken<Result<Double, Error>>() {
        }.getType();
        var balanceResponse = (Result<Double, Error>) Json.get().fromJson(response.getBody(), typeGetAccBalance);
        // assert
        Assertions.assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        Assertions.assertEquals(1000.0, balanceResponse.value());
    }

    @ParameterizedTest(name = "{arguments} test IBAN")
    @ValueSource(strings = {"DE45120300000000202053", "DE18120300000000202054"
            , "DE60120300000000202074"})
    public void transferMoneyMock(String iban) {
        // arrange
        String url = "/api/accounts/1/send";
        Iban ibanInst = org.iban4j.Iban.valueOf(iban);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String sendMoneyData = """
                {
                "target": {
                "iban": "%s"
                },
                "amount": 1000.0
                }""".formatted(iban);
        HttpEntity<String> entity = new HttpEntity<>(sendMoneyData, headers);
        // act
        var response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        // assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @ParameterizedTest(name = "IBAN {0} responses with {1}")
    @MethodSource("data")
    public void transferMoneyExternMock(String iban, HttpStatus expectedStatus
    ) {
        // arrange
        String url = "/api/accounts/1/sendexternal";
        //Iban ibanInst = org.iban4j.Iban.valueOf(iban);
        stubFor(get("/validator/validate?iban=" + iban)
                .willReturn(ok()));
        //.withHeader("Content-Type", "text/xml")
        //.withBody("<response>SUCCESS</response>")));
        headers.setContentType(MediaType.APPLICATION_JSON);
        String sendMoneyData = """
                {
                "target": {
                "iban": "%s"
                },
                "amount": 1000.0
                }""".formatted(iban);
        HttpEntity<String> entity = new HttpEntity<>(sendMoneyData, headers);
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
                Arguments.arguments("DE60120300000000202052", HttpStatus.INTERNAL_SERVER_ERROR),
                Arguments.arguments("DE60120300000000202051", HttpStatus.INTERNAL_SERVER_ERROR),
                Arguments.arguments("DE60120300000000202", HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }
}