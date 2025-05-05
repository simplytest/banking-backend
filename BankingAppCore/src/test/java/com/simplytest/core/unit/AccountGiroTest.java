package com.simplytest.core.unit;

import com.simplytest.core.accounts.AccountGiro;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccountGiroTest {

    @Test
    public void happyPathSendMoney() {
        double initBalance = 500.00;
        double amount = 51.23;

        AccountGiro accountGiro = new AccountGiro();
        accountGiro.setBalance(initBalance);
        accountGiro.sendMoney(amount, null);
        Assertions.assertEquals(initBalance - amount, accountGiro.getBalance());
    }
}
