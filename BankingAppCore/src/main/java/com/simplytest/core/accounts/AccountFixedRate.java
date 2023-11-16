package com.simplytest.core.accounts;

import com.simplytest.core.Error;

import org.iban4j.Iban;

import com.simplytest.core.utils.Result;

public class AccountFixedRate extends Account
{
    private double runtime;

    public AccountFixedRate()
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
        return AccountType.FixedRateAccount;
    }

    @Override
    public Result<Error> sendMoney(double amount, Iban target)
    {
        return Result.error(Error.NotSupported);
    }
}
