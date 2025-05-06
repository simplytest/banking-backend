package com.simplytest.server.integration;


import com.simplytest.server.auth.JWT;
import com.simplytest.server.data.DummyContract;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractControllerWithDBTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ContractRepository contractRepository;

    public void getContractInformation() {
        String url = "/api/contracts";
        var dummyContract = new DummyContract();
        var dbContract = contractRepository.save(new DBContract(dummyContract.createDummyContract()));
        String jwt = JWT.generate(dbContract.id());
    }

}
