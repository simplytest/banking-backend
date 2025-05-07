package com.simplytest.server.integration;

import com.google.gson.JsonObject;
import com.simplytest.server.BankingServer;
import com.simplytest.server.api.ContractController;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.json.Json;

import com.simplytest.server.repo.ContractRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ContractControllerMockMvcTest {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private BankingServer bankingServer;

    @Autowired
    private ContractController contractController;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getCurrentBalanceHappyPath() throws Exception {
        // arrange
        String url = "/api/contracts/login/1";
        String password = "demo";
        String jwtToken = JWT.generate(1L);
        // act + assert
        var response = mockMvc.perform(post(url).content(password).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        var parsed = Json.get().fromJson(response.getResponse().getContentAsString(), JsonObject.class);
        Assertions.assertEquals(JWT.getId(jwtToken), JWT.getId(parsed.get("result").getAsString()));

    }
}
