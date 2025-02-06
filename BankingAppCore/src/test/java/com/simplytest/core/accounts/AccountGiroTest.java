package com.simplytest.core.accounts;

import com.simplytest.core.Error;
import com.simplytest.core.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AccountGiroTest {
    AccountGiro account;

    // Alter Referenztest, als Herleitung OK, aber es fehlen wichtige Elemente
    // So wie der Test geschrieben ist, überprüft er noch nichts, keine Assertions!
    // Für mehrere Tests mit gleichem Start, @BeforeEach verwenden
//    @Test
//    public void testAccountAmmountSane() {
//        AccountGiro account = new AccountGiro();
//        account.setBalance(100);
//
//        account.setInterestRate(3.09);
//        Assertions.assertEquals(3.09, account.getInterestRate());
//        account.sendMoney(0, null);
//    }

    @BeforeEach
    public void setUp() {
        account = new AccountGiro();
        account.setBalance(100);
    }

    @Test
    public void testAccountAmmountSane() {
        account.setInterestRate(3.09);
        Assertions.assertEquals(3.09, account.getInterestRate());
        Result<Error> error = account.sendMoney(0, null);
        Assertions.assertFalse(error.successful());
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.0, -1.0, 55.475, 123.46 })
    public void testSendMoneyFails(double amount) {
        Result<Error> error = account.sendMoney(amount, null);
        Assertions.assertFalse(error.successful());
    }

    @ParameterizedTest
    @ValueSource(doubles = { 7.0, 10.0, 55.45, 93.46 })
    public void testSendMoneyGood(double amount) {
        Result<Error> error = account.sendMoney(amount, null);
        Assertions.assertTrue(error.successful());
    }

    @Test
    public void testSendMoneyDispoLimit() {
        account.setDispoLimit(100);
        account.setDispoRate(0.1);
        Result<Error> error = account.sendMoney(200, null);
        Assertions.assertTrue(error.successful());
        error = account.sendMoney(220, null);
        Assertions.assertFalse(error.successful());
        Assertions.assertTrue(account.getBalance() == -100);
    }
}