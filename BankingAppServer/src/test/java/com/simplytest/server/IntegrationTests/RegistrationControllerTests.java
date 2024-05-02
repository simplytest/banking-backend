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
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Calendar;

import static com.simplytest.core.accounts.AccountType.getType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistrationControllerTests {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ContractRepository contractRepository;


  //Übungsziel:
  // erstmaliges verwenden von Test Rest Template
  // Nutzung vom autowired Annotation
  // zeigen, dass Request verarbeitet wird und ein HTTP Status zurückommt und ein Body
  // -> ggf. zeigen, dass Debugpunkte im Controller gesetzt werden können
  // -> an dieser Stelle noch keine Auswertung vom "Json"
  @Test
  void uebung1_canCreateNewContract() {
    var customer = createDummyCustomer();
    //send request
    var restResponse = restTemplate.postForEntity("/api/contracts", customer, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(201), restResponse.getStatusCode());

  }

  //Übungsziel:
  // Zugriff auf internes Repository über Autowired Annotation
  //
  @Test
  void uebung2_canCreateNewContractWithAssertionBody() {
    var customer = createDummyCustomer();
    final var initial = contractRepository.count();

    //send request
    var restResponse = restTemplate.postForEntity("/api/contracts", customer, String.class);

    //validate response
    Assertions.assertEquals(HttpStatusCode.valueOf(201), restResponse.getStatusCode());
    Assertions.assertEquals(contractRepository.count(), initial + 1);

    // Parsen vom Response ins DTO
    final var parsedRequest =
        (Result<ContractRegistrationResult, Error>)
            Json.get().fromJson(restResponse.getBody(),
                TypeToken.getParameterized(Result.class, ContractRegistrationResult.class, Error.class));

    // Suchen vom Contract im Repo mit der generierten Id
    var contract = contractRepository.findById(parsedRequest.value().id()).get();
    // Prüfung ob die Daten richtig im Repository gespeichert wurden
    Assertions.assertEquals(customer.lastName, contract.value().getCustomer().getLastName());
  }


  //Übungsziel:
  // 0. pragmatisches Vorbereiten von Testdaten anhand echter Backend Calls (vs. Anlage als DTOs)
  // 1. pragmatische Anlage von Testdaten direkt über das Repository (skip der API Anlage)
  // 2. Anfrage des Services über Rest mit "Auth" Hintergrund -> fail
  // 2.1 JWT zusammenstellen -> über Testfall davor -> Anfragen bzw interne Methode für die Erstellung nutzen
  // 3. auf Unterschiede von getForEntity, exchange und execute methoden von restTemplate eingehen
  @Test
  void uebung3_canRequestContractInformation() {
    contractRepository.save(new DBContract(createDummyContract()));

    //generate jwt Token for access
    String jwtToken = JWT.generate(contractRepository.findAll().get(0).value().getId());
    String url = "/api/contracts";

    var headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> entity = new HttpEntity<>(headers);

    // Führe die GET-Anfrage mit der JWT-Token-HttpEntity aus
    var result = restTemplate.exchange(
        url, HttpMethod.GET, entity, String.class
    );

    var parsed = Json.get().fromJson(result.getBody(), TypeToken.get(Contract.class));

    //prüfe Giro Konto ist vorhanden
    var account = parsed.getAccounts().entrySet().stream()
        .filter(x -> x.getValue().getType() == getType("Giro Konto")).findFirst();

    Assertions.assertTrue(account.isPresent(), "Giro Konto exists");
  }

  //Übung 4: Vertrag kündigen
  // Wiederholung vom gelernten am neuen Endpunkt, keine neuen Inhalte
  @Test
  void uebung4_canCancelContract() {
    contractRepository.save(new DBContract(createDummyContract()));

    String jwtToken = JWT.generate(contractRepository.findAll().get(0).value().getId());
    String url = "/api/contracts";

    var headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> entity = new HttpEntity<>(headers);

    var result = restTemplate.exchange(
        url, HttpMethod.DELETE, entity, String.class
    );

    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    Assertions.assertEquals(0, contractRepository.count());


  }


  //Übung 5: Immobilienfinanzierung abschließen
  // Wiederholung:
  //  + aufbau komplexerer Szenarien
  //  + Auswertung komplexerer Rückgabewerte
  @Test
  void uebung5_createRealtyContract() {
    contractRepository.save(new DBContract(createDummyContract()));

    String jwtToken = JWT.generate(contractRepository.findAll().get(0).value().getId());

    String url = "/api/contracts/accounts";
    var headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    String body = Json.get().toJson(new RealEstateAccount(0.04, 8000));
    HttpEntity<String> entity = new HttpEntity<>(body, headers);

    var result = restTemplate.exchange(
        url, HttpMethod.POST, entity, String.class
    );

    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());

    //check repository
    var account = contractRepository.findAll().get(0).value().getAccounts().entrySet().stream()
        .filter(x -> x.getValue().getType() == getType("Immobilien-Finanzierungskonto")).findFirst();

    var immoaccount = ((AccountRealEstate) (account.get()).getValue());

    Assertions.assertEquals(-8000.00, immoaccount.getBalance());
    Assertions.assertEquals(26.666666666666668, immoaccount.getMonthlyAmount());

  }

  // Übung 6 -> siehe TransferMoney ControllerTests.java



  //Übung 8: Interationstests mit mehreren API endpunkten
  // Fachlicher Ablauf:
  // Anlage vom Contract über Rest
  // Abfrage vom Contract über Rest  -> Validierung der Informationen
  // Kündigung vom Contract über Rest
  // Prüfung der erfolgreichen Löschung über Rest -> Rückgabewert 404
  @Test
  void uebung8_contractIntegrationTest() {

    // - - - - - - - - - - - - - - - - - - -
    //1. Anlage contract für Kunden
    var customer = createDummyCustomer();

    //send request
    var createResponse = restTemplate.postForEntity("/api/contracts", customer, String.class);

    Assertions.assertEquals(HttpStatusCode.valueOf(201), createResponse.getStatusCode());

    var id = contractRepository.findAll().get(0).value().getId();

    // - - - - - - - - - - - - - - - - - - -
    // 2. Abfrage des Accounts im Backend über Rest
    String jwtToken = JWT.generate(id);
    String url = "/api/contracts";

    var headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> entity = new HttpEntity<>(headers);

    // Führe die GET-Anfrage mit der JWT-Token-HttpEntity aus
    var infoResponse = restTemplate.exchange(
        url, HttpMethod.GET, entity, String.class
    );

    var parsed = Json.get().fromJson(infoResponse.getBody(), TypeToken.get(Contract.class));
    Assertions.assertEquals(HttpStatusCode.valueOf(200), infoResponse.getStatusCode());
    Assertions.assertEquals(customer.firstName, parsed.getCustomer().getFirstName());

    // - - - - - - - - - - - - - - - - - - -
    // 3. delete customer über Rest

    var resultDelete = restTemplate.exchange(
        url, HttpMethod.DELETE, entity, String.class
    );

    Assertions.assertEquals(HttpStatusCode.valueOf(200), resultDelete.getStatusCode());


    // - - - - - - - - - - - - - - - - - - -
    // 4. Abfrage des Accounts im Backend über Rest

    infoResponse = restTemplate.exchange(
        url, HttpMethod.GET, entity, String.class
    );

    Assertions.assertEquals(HttpStatusCode.valueOf(404), infoResponse.getStatusCode());
  }

  //Variante mit JSON Anlage
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
        "          \"balance\": 0.0,\n" +
        "          \"boundPeriod\": 0.0,\n" +
        "          \"interestRate\": 0.0\n" +
        "        }\n" +
        "      }\n" +
        "    ]\n" +
        "  ]\n" +
        "}";

    return Json.get().fromJson(dummy, Contract.class);

  }

  //Variante Testdata Anlage über DTO
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
