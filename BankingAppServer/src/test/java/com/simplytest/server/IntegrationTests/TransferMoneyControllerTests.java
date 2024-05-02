package com.simplytest.server.IntegrationTests;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.core.accounts.AccountRealEstate;
import com.simplytest.core.contracts.Contract;
import com.simplytest.core.customers.Address;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.apiData.RealEstateAccount;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.APIUtil;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Calendar;
import java.util.Optional;

import static com.simplytest.core.accounts.AccountType.getType;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransferMoneyControllerTests {

  @Autowired
  private TestRestTemplate restTemplate;

  @MockBean
  private ContractRepository contractRepository;


  //Übung 6: Transfer Money
  // Lernziele: Isolierung der Persistierungsschicht mit Hilfe von MockBeans
  //  - zeigen, dass MockBean das Repository im Debug Zustand austauscht
  //  - zeigen von Unterschied zu Rest Assured / Postman

  // -> aktives Isolieren der Datenhaltungsschicht und Konzentration auf Prüfung von Controller Logik
  @Test
  void uebung6_canTransferMoney() {
    var dummyContract = createDummyContract();

    when(contractRepository.findById(dummyContract.getId().parent()))
        .thenReturn(Optional.of(new DBContract(dummyContract)));

    String jwtToken = JWT.generate(dummyContract.getId());
    String url = String.format("/api/accounts/%d/balance",dummyContract.getId().parent());

    var headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> entity = new HttpEntity<>(headers);

    var result = restTemplate.exchange(
        url,
        HttpMethod.GET,
        entity,
        String.class
    );

    var parsed = (Result<Double, Error>)Json.get().fromJson(result.getBody(), TypeToken.getParameterized(Result.class, Double.class, Error.class));
    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    Assertions.assertEquals(12.00, parsed.value());

  }

  private Contract createDummyContract() {

    String dummy = "{\n" +
        "  \"id\": {\n" +
        "    \"counter\": 1,\n" +
        "    \"parent\": 1,\n" +
        "    \"child\": 0\n" +
        "  },\n" +
        "  \"customer\": {\n" +
        "    \"type\": \"com.simplytest.core.customers.CustomerPrivate\",\n" +
        "    \"data\": {\n" +
        "      \"birthDay\": \"Feb 1, 2000, 10:21:56 PM\",\n" +
        "      \"schufaScore\": 0.0,\n" +
        "      \"transactionFee\": 0.0,\n" +
        "      \"monthlyFee\": 2.99,\n" +
        "      \"firstName\": \"Foo\",\n" +
        "      \"lastName\": \"Bar\",\n" +
        "      \"address\": {\n" +
        "        \"country\": \"Deutschland\",\n" +
        "        \"zipCode\": \"12345\",\n" +
        "        \"street\": \"Some Street\"\n" +
        "      }\n" +
        "    }\n" +
        "  },\n" +
        "  \"passwordHash\": \"$2a$12$YkHRZthCXrh/OoXy7QG1suKccQ6pkIh8UlStfWPpbouqOVD0UcEyK\",\n" +
        "  \"accounts\": [\n" +
        "    [\n" +
        "      {\n" +
        "        \"counter\": 1,\n" +
        "        \"parent\": 1,\n" +
        "        \"child\": 1\n" +
        "      },\n" +
        "      {\n" +
        "        \"type\": \"com.simplytest.core.accounts.AccountGiro\",\n" +
        "        \"data\": {\n" +
        "          \"sendLimit\": 3000.0,\n" +
        "          \"dispoLimit\": 0.0,\n" +
        "          \"dispoRate\": 0.0,\n" +
        "          \"balance\": 12.0,\n" +
        "          \"boundPeriod\": 0.0,\n" +
        "          \"interestRate\": 0.0\n" +
        "        }\n" +
        "      }\n" +
        "    ]\n" +
        "  ]\n" +
        "}";

    return Json.get().fromJson(dummy, Contract.class);

  }
}
