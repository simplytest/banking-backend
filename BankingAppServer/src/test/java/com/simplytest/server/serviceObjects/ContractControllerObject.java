package com.simplytest.server.serviceObjects;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.customers.Customer;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.data.DummyCustomer;
import com.simplytest.server.json.Json;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Type;

public class ContractControllerObject {

    private TestRestTemplate restTemplate;
    private String contractUrl = "/api/contracts";

    public ContractControllerObject(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> createDefaultCustomer() {
        System.out.println("Creating default customer");
        var dummyCustomer = new DummyCustomer();
        var customer = dummyCustomer.createDefaultCustomerDTO();
        ResponseEntity<String> response = restTemplate.postForEntity(contractUrl, customer, String.class);
        return response;
    }
    public ResponseEntity<String> createCustomer(CustomerData customerData) {
        System.out.println("Creating specific customer");
        ResponseEntity<String> response = restTemplate.postForEntity(contractUrl, customerData, String.class);
        return response;
    }
    public String validateCustomerCreation(ResponseEntity<String> response) {
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Type contractResponseType = new TypeToken<Result<ContractRegistrationResult, Error>>() {
        }.getType();
        @SuppressWarnings("unchecked")
        final var parsed = (Result<ContractRegistrationResult, Error>) Json.get().fromJson(response.getBody(),
                contractResponseType);
        System.out.println(parsed.value().id());
        System.out.println(parsed.value().JWT());
        String jwtToken = parsed.value().JWT();
        return jwtToken;
    }

    public DBContract createDefaultCustomerInDB(ContractRepository contractRepository) {
        var dummyContract = new DummyContract();
        return contractRepository.save(new DBContract(dummyContract.createDummyContract()));
    }

    public DBContract createCustomerInDB(ContractRepository contractRepository, String firstName, String lastName) {
        var dummyContract = new DummyContract();
        var contract = dummyContract.createDummyContract();
        contract = dummyContract.changeCustomerData(contract, firstName, lastName);
        return contractRepository.save(new DBContract(contract));
    }
}
