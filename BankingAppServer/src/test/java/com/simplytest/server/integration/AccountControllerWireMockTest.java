package com.simplytest.server.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.gson.reflect.TypeToken;
import com.simplytest.server.apiData.Iban;
import com.simplytest.server.apiData.SendMoney;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import com.simplytest.core.Error;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.simplytest.core.accounts.AccountType.getType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@WireMockTest(httpPort = 8081)
public class AccountControllerWireMockTest {
    private DBContract myContract;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ContractRepository contractRepository;

    @BeforeEach
    public void createCustomerInDatabase() {
        DummyContract prepContract = new DummyContract();
        myContract = contractRepository.save(new DBContract( prepContract.createDummyContract()));
    }
    @Test
    public void testTransferMoneyExternalGood() {
        String iban = "DE02120300000000202051";
        stubFor(get("/validator/validate?iban=" + iban).willReturn(ok()));

        String jwtToken = JWT.generate(myContract.id());
        var accountID = myContract.value().getAccounts().entrySet().stream()
                .filter(x -> x.getValue().getType() == getType("Giro Konto")).findFirst().get().getKey().parent();
        String url = String.format("/api/accounts/%d/sendexternal", 1);

        var accounts = myContract.value().getAccounts();

        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

//        String body = Json.get().toJson(new SendMoney(new Iban(iban), 12));
        HttpEntity<String> entity = new HttpEntity<>(Json.get().toJson(new SendMoney(new Iban(iban), 12)), headers);
        System.out.println("entity: " + entity);

        var result = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class);

        var parsedResult = (Result<Boolean, Error>) Json.get().fromJson(result.getBody(),
                TypeToken.getParameterized(Result.class, Boolean.class, Error.class));
        Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
        Assertions.assertTrue(parsedResult.value());
    }
}
