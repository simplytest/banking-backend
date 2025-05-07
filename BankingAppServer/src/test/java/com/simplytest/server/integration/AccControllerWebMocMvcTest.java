package com.simplytest.server.integration;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.simplytest.server.api.ContractController;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.model.DBContract;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class AccControllerWebMocMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private String jwtToken;
    private DBContract myDBContract;

    @BeforeEach
    public void setup() {
        // Test-Daten vorbereiten
        final long anId = 1L;
        jwtToken = JWT.generate(anId);
        // Mock-Verhalten konfigurieren
}

    @Test
    public void getBalanceTest() throws Exception {
        // Test für die Balance-Abfrage
        var response = mockMvc.perform(get("/api/accounts/1/balance")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

        System.out.println(response.andReturn().getResponse().getContentAsString());
        JsonObject jsonObject = new Gson().fromJson(response.andReturn().getResponse().getContentAsString(), JsonObject.class);
        double balance = jsonObject.get("result").getAsDouble();
        Assertions.assertTrue(balance > 0);
    }
}