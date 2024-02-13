package com.simplytest.server.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.google.gson.reflect.TypeToken;
import com.simplytest.server.BankingServer;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.json.Json;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;

@SpringBootTest
@AutoConfigureMockMvc
class RegisterSpringTest
{
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ContractRepository contractRepository;

        public <R> R parse(String json, TypeToken<?> token)
        {
                return (R) Json.get().fromJson(json, token);
        }

        @Test
        void createContract() throws Exception
        {
                final var user = BankingServer.createDemoUser("SpringTest",
                                "password");

                final var initial = contractRepository.count();

                final var response = mockMvc
                                .perform(post("/api/contracts")
                                                .content(Json.get().toJson(user))
                                                .contentType("application/json")
                                                .param("initialBalance", "1337"))
                                .andExpect(status().isCreated()).andReturn()
                                .getResponse().getContentAsString();

                Assertions.assertEquals(contractRepository.count(), initial + 1);

                final var responseType = TypeToken.getParameterized(Result.class,
                                ContractRegistrationResult.class, Error.class);

                final var parsed = this
                                .<Result<ContractRegistrationResult, Error>> parse(
                                                response, responseType);

                Assertions.assertTrue(parsed.successful());

                final var result = parsed.value();

                final var contract = contractRepository.findById(result.id()).get()
                                .value();

                Assertions.assertEquals(contract.getCustomer().getLastName(),
                                "SpringTest");

                var accounts = contract.getAccounts().values().stream().toList();

                Assertions.assertEquals(accounts.size(), 1);
                Assertions.assertEquals(accounts.get(0).getBalance(), 1337);
        }
}
