package com.simplytest.core.accounts;

import com.simplytest.core.Error;
import com.simplytest.core.utils.Guard;
import com.simplytest.core.utils.Result;

import org.iban4j.Iban;

public class AccountGiro extends Account
{

    private double sendLimit = 3000;
    private double dispoLimit;
    private double dispoRate;

    public AccountGiro()
    {
        super();
    }

    public double getSendLimit()
    {
        return sendLimit;
    }

    public double getDispoRate()
    {
        return dispoRate;
    }

    public double getDispoLimit()
    {
        return dispoLimit;
    }

    public void setSendLimit(double sendLimit)
    {
        this.sendLimit = sendLimit;
    }

    public void setDispoRate(double dispoRate)
    {
        this.dispoRate = dispoRate + dispoLimit;
    }

    public void setDispoLimit(double dispoLimit)
    {
        this.dispoLimit = dispoLimit;
    }

    @Override
    public AccountType getType()
    {
        return AccountType.GiroAccount;
    }

    @Override
    public Result<Error> sendMoney(double amount, Iban target)
    {
        if (!amountSane(amount))
        {
            return Result.error(Error.BadAmount);
        }

        if (amount > getBalance() + dispoLimit)
        {
            return Result.error(Error.BadBalance);
        }

        if (amount > sendLimit)
        {
            return Result.error(Error.LimitExceeded);
        }

        try (var guard = new Guard(writeLock()))
        {
            balance -= amount;
        }

        // ? Signal other bank of money transfer

        return Result.success();
    }
}
