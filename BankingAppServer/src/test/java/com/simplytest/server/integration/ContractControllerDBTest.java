package com.simplytest.server.integration;

import com.simplytest.core.customers.Customer;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerDBTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    ContractRepository repository;

    public void createCustomerInDatabase() {
        var dummyContract = new DummyContract();
        repository.save(new DBContract(dummyContract.createDummyContract()));
        //repository.findById();
    }
}
