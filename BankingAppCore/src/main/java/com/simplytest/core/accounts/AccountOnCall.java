package com.simplytest.core.accounts;

import com.simplytest.core.Error;
import com.simplytest.core.utils.Result;

import org.iban4j.Iban;

import com.simplytest.core.AccountType;

public class AccountOnCall extends Account
{
    private double runtime;

    public AccountOnCall()
    {
        super();
    }

    public double getRuntime()
    {
        return runtime;
    }

    public void setRuntime(double runtime)
    {
        this.runtime = runtime;
    }

    @Override
    public AccountType getType()
    {
        return AccountType.OnCallAccount;
    }

    @Override
    public Result<Error> sendMoney(double amount, Iban target)
    {
        return Result.error(Error.NotSupported);
    }
}
