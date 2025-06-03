package com.simplytest.server.integration;

import com.simplytest.server.data.DummyContract;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerMockTest {

    @MockitoBean
    ContractRepository contractRepository;

    private DBContract myDBContract;

    @BeforeEach
    public void setup() {
        final long anId = 1L;
        var dummyContrac = new DummyContract();
        String url = "/api/accounts/1/balance";
        myDBContract = new DBContract(dummyContrac.createDummyWithRE());
        when(contractRepository.findById(anId)).thenReturn(Optional.of(myDBContract));

    }
}
