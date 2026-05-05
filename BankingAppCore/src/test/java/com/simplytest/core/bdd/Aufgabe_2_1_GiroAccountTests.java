package com.simplytest.core.bdd;

import com.simplytest.core.Error;
import com.simplytest.core.accounts.AccountGiro;
import com.simplytest.core.utils.Result;
import org.iban4j.Iban;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class Aufgabe_2_1_GiroAccountTests {

    @Test
    public void testGiroAccountCreation() {
        AccountGiro account = new AccountGiro();
        account.setBalance(1000);
        Result<Error> result = account.sendMoney(100.0, Iban.random());

        Assertions.assertTrue(result.successful(), "GiroAccount should be able to send money");
        Assertions.assertEquals(900.0, account.getBalance(), "GiroAccount should have a reduced balance");
    }

    @Test
    public void testGiroAccountWithNegativeBalance() {
        AccountGiro account = new AccountGiro();
        account.setBalance(-1000);
        Result<Error> result = account.sendMoney(100.0, Iban.random());

        Assertions.assertFalse(result.successful(), "GiroAccount should not be able to send money");
        Assertions.assertEquals(Error.BadBalance, result.error());
        Assertions.assertEquals(-1000.0, account.getBalance(), "GiroAccount should not have a reduced balance");
    }


    @ParameterizedTest
    @CsvSource({
        "0.0,   100",
        "55.475, 100.0",
        "123.46, 100.0",
        "-1.0,   100.0"
    })
    public void testSendMoneyFails(double amount, double balance) {
        AccountGiro giroAccount = new AccountGiro();
        giroAccount.setBalance(balance);
        Result<Error> result = giroAccount.sendMoney(amount, Iban.random());

        Assertions.assertFalse(result.successful(), "Sending money should have failed for amount: " + amount);
        Assertions.assertEquals(balance, giroAccount.getBalance(), "Balance should remain unchanged after failed send: " + result.error().name());
    }
}
