package com.simplytest.server.integrationTests.accountAPI;

import com.google.gson.reflect.TypeToken;

import com.simplytest.server.auth.JWT;
import com.simplytest.server.integrationTests.utils.ContractUtils;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import com.simplytest.core.Error;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountController_DB_Injection_Test {

    static final private String BASE_URL = "/api/accounts";
    static final private double INITIAL_BALANCE = 500.0;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ContractRepository contractDB;

    private String jwt;
    private long accountId;

    @BeforeEach
    public void setup() {
        DBContract dbEntry = ContractUtils.registerNewContractInDB(contractDB, "TestUser", "password", INITIAL_BALANCE);

        jwt = JWT.generate(dbEntry.id());

        // Die erste (und einzige) Account-ID aus dem Vertrag ermitteln
        accountId = dbEntry.value().getAccounts().keySet().stream().findFirst().get().child();
    }

    @Test
    public void getBalanceTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", jwt);

        var response = restTemplate.exchange(
                BASE_URL + "/" + accountId + "/balance",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        var result = Json.get().fromJson(response.getBody(), new TypeToken<Result<Double, Error>>() {});
        Assertions.assertTrue(result.successful());
        Assertions.assertEquals(INITIAL_BALANCE, result.value());
    }

    @Test
    public void receiveMoneyTest() {
        final double receiveAmount = 200.0;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", jwt);

        var response = restTemplate.exchange(
                BASE_URL + "/" + accountId + "/receive?amount=" + receiveAmount,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        var result = Json.get().fromJson(response.getBody(), new TypeToken<Result<Boolean, Error>>() {});
        Assertions.assertTrue(result.successful());

        // Gegenprüfung: Kontostand über Balance-Endpunkt verifizieren
        var balanceResponse = restTemplate.exchange(
                BASE_URL + "/" + accountId + "/balance",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        var balanceResult = Json.get().fromJson(balanceResponse.getBody(), new TypeToken<Result<Double, Error>>() {});
        Assertions.assertEquals(INITIAL_BALANCE + receiveAmount, balanceResult.value());
    }

    @Test
    public void sendMoneyTest() {
        final double sendAmount = 100.0;
        // Gültige deutsche Test-IBAN
        final String targetIban = "DE89370400440532013000";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", jwt);

        var body = String.format("{ \"target\": { \"iban\": \"%s\" }, \"amount\": %s }", targetIban, sendAmount);

        var response = restTemplate.exchange(
                BASE_URL + "/" + accountId + "/send",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        var result = Json.get().fromJson(response.getBody(), new TypeToken<Result<Boolean, Error>>() {});
        Assertions.assertTrue(result.successful());

        // Gegenprüfung: Kontostand wurde reduziert
        var balanceResponse = restTemplate.exchange(
                BASE_URL + "/" + accountId + "/balance",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        var balanceResult = Json.get().fromJson(balanceResponse.getBody(), new TypeToken<Result<Double, Error>>() {});
        Assertions.assertEquals(INITIAL_BALANCE - sendAmount, balanceResult.value());
    }

    @Test
    public void receiveMoneyVerifiedInDBTest() {
        final double receiveAmount = 300.0;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", jwt);

        var response = restTemplate.exchange(
                BASE_URL + "/" + accountId + "/receive?amount=" + receiveAmount,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        // Gegenprüfung direkt in der Datenbank
        var dbEntry = contractDB.findById(JWT.getId(jwt).get()).get();
        var accountInDB = dbEntry.value().getAccounts().values().stream().findFirst().get();
        Assertions.assertEquals(INITIAL_BALANCE + receiveAmount, accountInDB.getBalance());
    }

    @Test
    public void sendMoneyVerifiedInDBTest() {
        final double sendAmount = 150.0;
        final String targetIban = "DE89370400440532013000";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", jwt);

        var body = String.format("{ \"target\": { \"iban\": \"%s\" }, \"amount\": %s }", targetIban, sendAmount);

        var response = restTemplate.exchange(
                BASE_URL + "/" + accountId + "/send",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        // Gegenprüfung direkt in der Datenbank
        var dbEntry = contractDB.findById(JWT.getId(jwt).get()).get();
        var accountInDB = dbEntry.value().getAccounts().values().stream().findFirst().get();
        Assertions.assertEquals(INITIAL_BALANCE - sendAmount, accountInDB.getBalance());
    }
}
