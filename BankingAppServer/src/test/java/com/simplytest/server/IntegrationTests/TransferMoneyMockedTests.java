package com.simplytest.server.IntegrationTests;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.core.contracts.Contract;
import com.simplytest.core.customers.Address;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.apiData.Iban;
import com.simplytest.server.apiData.RealEstateAccount;
import com.simplytest.server.apiData.SendMoney;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.util.Calendar;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 8081)
@TestPropertySource("classpath:application-test.properties")
//@TestPropertySource(properties = {"validationurl=http://localhost:8081"})
public class TransferMoneyMockedTests {

  @Autowired
  private TestRestTemplate restTemplate;

  @MockBean
  private ContractRepository contractRepository;


  //Übung 7: transfer Money -> externer call
  // Lernziele: Einsatz von WireMock Proxy / Server
  // Austausch von application.properties durch Mock Server über TestPropertySource

  // ziel: Externe Calls abfangen um positive und negative szenarien zu testen
  @Test
  void uebung7_1_successfullTransferMoney() {
    String iban = "DE02120300000000202051";

    stubFor(get("/validator/validate?iban=" + iban).willReturn(ok()));

    var dummyContract = createDummyContract();
    when(contractRepository.findById(dummyContract.getId().parent())).thenReturn(Optional.of(new DBContract(dummyContract)));

    String jwtToken = JWT.generate(dummyContract.getId());
    String url = String.format("/api/accounts/%d/sendexternal", dummyContract.getId().parent());

    var headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
    headers.setContentType(MediaType.APPLICATION_JSON);


    String body = Json.get().toJson(new SendMoney(new Iban(iban), 12));
    HttpEntity<String> entity = new HttpEntity<>(body, headers);


    var result = restTemplate.exchange(
        url,
        HttpMethod.POST,
        entity,
        String.class
    );

    var parsedResult = (Result<Boolean, Error>)Json.get().fromJson(result.getBody(), TypeToken.getParameterized(Result.class, Boolean.class, Error.class));
    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    Assertions.assertTrue(parsedResult.value());

  }

  //test von sendmoney external mit ausgefallenem externen Validator
  @Test
  void uebung7_2_failingTransferMoney() {
    String iban = "DE02120300000000202051";

    stubFor(get("/validator/validate?iban=" + iban).willReturn(serverError()));

    var dummyContract = createDummyContract();
    when(contractRepository.findById(dummyContract.getId().parent())).thenReturn(Optional.of(new DBContract(dummyContract)));

    String jwtToken = JWT.generate(dummyContract.getId());
    String url = String.format("/api/accounts/%d/sendexternal", dummyContract.getId().parent());

    var headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
    headers.setContentType(MediaType.APPLICATION_JSON);


    String body = Json.get().toJson(new SendMoney(new Iban(iban), 12));
    HttpEntity<String> entity = new HttpEntity<>(body, headers);


    var result = restTemplate.exchange(
        url,
        HttpMethod.POST,
        entity,
        String.class
    );

    Assertions.assertEquals(HttpStatusCode.valueOf(400), result.getStatusCode());

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

  private CustomerData createDummyCustomer() {
    var customer = new CustomerData();
    customer.type = CustomerData.CustomerType.Private;

    var birthDay = Calendar.getInstance();
    birthDay.set(2000, 01, 01);

    customer.birthDay = birthDay.getTime();
    customer.address = new Address();
    customer.firstName = "Foo";
    customer.lastName = "Bar";

    customer.password = "password";

    customer.address.setStreet("Some Street");
    customer.address.setZipCode("12345");

    return customer;
  }
}
