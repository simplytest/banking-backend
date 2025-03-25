package com.simplytest.server.integration;

import com.google.common.reflect.TypeToken;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Type;
import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerMockTest {
    @Autowired
    TestRestTemplate restTemplate;

    @MockitoBean
    ContractRepository contractRepository;

    // Verwende BeforeEach, um nachfolgende Tests vorzubereiten
    // Schreibe Methoden, die die Endpunkte receibe, send und transfer testen.
    // Verwende ParameterizedTest, um die verschiedenen Fälle zu testen
    // Lege data und util packages an, um die Daten und Hilfsklassen zu speichern.
    // Um ein lauffähigs Beispiel zu haben, ist in der BankingServer ein Schritt auskommentiert
    //        controller.registerContract(createDemoUser("demo", "demo"), 1000.0,
    //                response);
    // was müssen wir hinzufügen um diese Zeilen in der Hauptapplikation belassen zu können?

    @Test
    public void getAccountBalanceMock() {
        var dummyContrac = new DummyContract();
        String url = "/api/accounts/1/balance";
        var dummyContract = new DBContract(dummyContrac.createDummyContract());

        when(contractRepository.findById(dummyContract.id())).thenReturn(Optional.of(dummyContract));
        String jwtToken = JWT.generate(dummyContract.id());

        var header = new HttpHeaders();
        header.set(HttpHeaders.AUTHORIZATION, jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(header);

        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Type typeGetAccBalance = new TypeToken<Result<Double, Error>>() {
        }.getType();
        var balanceRespone = (Result<Double, Error>) Json.get().fromJson(response.getBody(), typeGetAccBalance);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        Assertions.assertEquals(1000.0, balanceRespone.value());
    }

}
