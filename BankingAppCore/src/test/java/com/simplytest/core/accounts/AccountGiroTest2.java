package com.simplytest.core.accounts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountGiroTest2 {
    AccountGiro account;
    @BeforeEach
    void setUp() {
        account = new AccountGiro();
        account.setBalance(100);
        account.setInterestRate(3.09);
    }

    @Test
    void sendMoneyHappyPath() {
        var error = account.sendMoney(12.00, null);
        assertTrue(error.successful());
        assertTrue(account.getBalance() == 88.00);
    }

    @Test
    void sendMoneyInvalidAmmount() {
        var error = account.sendMoney(0, null);
        assertFalse(error.successful());
        assertTrue(account.getBalance() == 100.00);
    }
}