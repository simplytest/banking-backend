package com.simplytest.core;

import com.simplytest.core.accounts.AccountGiro;
import com.simplytest.core.utils.Result;
import org.iban4j.Iban;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class Aufgabe_2_1_GiroAccountTests {

    @Test
    public void testGiroAccountCreation() {
        // Test code for creating a Giro account
        AccountGiro giroAccount = new AccountGiro();
        giroAccount.setBalance(5000.0);
        Result<Error> result = giroAccount.sendMoney(100.0, Iban.random());

        Assertions.assertTrue(result.successful(), "Failed to create Giro account or send money");
        Assertions.assertEquals(4900, giroAccount.getBalance(), "Invalid balance after sending money");
    }


    @ParameterizedTest
    @ValueSource(doubles = { 0.0, 55.475, 123.46, -1.0  })
    public void testSendMoneyFails(double amount) {
        AccountGiro giroAccount = new AccountGiro();
        giroAccount.setBalance(100.0);
        Result<Error> result = giroAccount.sendMoney(amount, Iban.random());

        Assertions.assertFalse(result.successful(), "Sending money should have failed for amount: " + amount);
        Assertions.assertEquals(100.0-amount, giroAccount.getBalance(), "Balance should remain unchanged after failed send: " + result.error().name());
    }
}
