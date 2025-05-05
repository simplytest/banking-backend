package com.simplytest.core.unit;

import com.simplytest.core.Error;
import com.simplytest.core.accounts.AccountGiro;
import com.simplytest.core.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class AccountGiroTest {

    @Test
    public void happyPathSendMoney() {
        double initBalance = 500.00;
        double amount = 51.23;

        AccountGiro accountGiro = new AccountGiro();
        accountGiro.setBalance(initBalance);
        Result<Error> error =  accountGiro.sendMoney(amount, null);
        Assertions.assertTrue(error.successful());
        Assertions.assertEquals(initBalance - amount, accountGiro.getBalance());
    }

    @Test
    public void amountTooBig() {
        double initBalance = 100.00;
        double amount = 353.23;
        double dispoLimit = 200.00;

        AccountGiro accountGiro = new AccountGiro();
        accountGiro.setBalance(initBalance);
        accountGiro.setDispoLimit(dispoLimit);
        Result<Error> error =  accountGiro.sendMoney(amount, null);
        Assertions.assertFalse(error.successful());
        Assertions.assertEquals(initBalance, accountGiro.getBalance());
        System.out.println(error.error().name());
        Assertions.assertEquals("BadBalance", error.error().name());
    }

    @Test
    public void sendLimitExeeded() {
        double initBalance = 100.00;
        double amount = 35.23;
        double dispoLimit = 200.00;

        AccountGiro accountGiro = new AccountGiro();
        accountGiro.setBalance(initBalance);
        accountGiro.setDispoLimit(dispoLimit);
        accountGiro.setSendLimit(32.00);
        Result<Error> error =  accountGiro.sendMoney(amount, null);
        Assertions.assertFalse(error.successful());
        Assertions.assertEquals(initBalance, accountGiro.getBalance());
        System.out.println(error.error().name());
        Assertions.assertEquals("LimitExceeded", error.error().name());
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.0, -1.0, 55.475, 123.46 })
    public void testSendMoneyFails(double amount) {
        AccountGiro accountGiro = new AccountGiro();
        accountGiro.setBalance(100);
        Result<Error> error = accountGiro.sendMoney(amount, null);
        Assertions.assertFalse(error.successful());
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, BadAmount",
            "-1.0, BadAmount",
            "55.475, BadAmount",
            "123.46, BadBalance",
            "70.00, LimitExceeded",
    })
    public void testSendMoneyFails(double amount, Error expectedError) {
        AccountGiro accountGiro = new AccountGiro();
        accountGiro.setBalance(100);
        accountGiro.setSendLimit(45.00);
        Result<Error> error = accountGiro.sendMoney(amount, null);
        Assertions.assertFalse(error.successful());
        Assertions.assertEquals(expectedError, error.error());
    }
}
