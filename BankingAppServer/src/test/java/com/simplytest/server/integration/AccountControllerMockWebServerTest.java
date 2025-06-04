package com.simplytest.server.integration;

import com.simplytest.server.repo.ContractRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerMockWebServerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    ContractRepository repository;

    private MockWebServer mockExternalService;

    @BeforeEach
    public void setUp() throws IOException {
        mockExternalService = new MockWebServer();
        mockExternalService.start(8081);
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockExternalService.shutdown();
    }

    public void happyPathSendExternal() {
        mockExternalService.enqueue(new MockResponse().setResponseCode(200));
    }

}
