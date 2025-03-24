package com.simplytest.core.unitTest;

import com.simplytest.core.Error;
import com.simplytest.core.accounts.AccountGiro;
import com.simplytest.core.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountGiroTest {

    @Test
    public void happyPathSendMoney() {
        AccountGiro accountGiro = new AccountGiro();
        accountGiro.setBalance(500);
        accountGiro.sendMoney(51.23, null);
        assertEquals(448.77, accountGiro.getBalance());
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.0, -1.0, 55.475, 123.46 })
    public void testSendMoneyFails(double amount) {
        AccountGiro accountGiro = new AccountGiro();
        accountGiro.setBalance(100);
        Result<Error> error = accountGiro.sendMoney(amount, null);
        Assertions.assertFalse(error.successful());
    }

}
