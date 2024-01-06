package com.simplytest.core.accounts;

import com.simplytest.core.Error;
import com.simplytest.core.utils.Guard;
import com.simplytest.core.utils.Result;

import org.iban4j.Iban;

public class AccountRealEstate extends Account
{
    private double remainingAmount;

    private double creditAmount;
    private double payedAmount;
    private double maxSpecialRepayment;

    private double runtimeAmount;
    private double repaymentRate;
    private double monthlyAmount;

    protected AccountRealEstate()
    {
        super();
    }

    public AccountRealEstate(double repaymentRate, double creditAmount)
    {
        super();

        this.repaymentRate = repaymentRate;
        this.creditAmount = creditAmount;

        this.maxSpecialRepayment = this.creditAmount * 0.05;

        setBalance(-creditAmount);
    }

    public double getMonthlyAmount()
    {
        return monthlyAmount;
    }

    public double getPayedAmount()
    {
        return payedAmount;
    }

    public double getCreditAmount()
    {
        return creditAmount;
    }

    public double getRuntimeAmount()
    {
        return runtimeAmount;
    }

    public double getRemainingAmount()
    {
        return remainingAmount;
    }

    public void setPayedAmount(double payedAmount)
    {
        this.payedAmount = payedAmount;
    }

    public void setMonthlyAmount(double monthlyAmount)
    {
        this.monthlyAmount = monthlyAmount;
    }

    public void setCreditAmount(double creditAmount)
    {
        this.creditAmount = creditAmount;
    }

    public void setRuntimeAmount(double runtimeAmount)
    {
        this.runtimeAmount = runtimeAmount;
    }

    public void setRemainingAmount(double remainingAmount)
    {
        this.remainingAmount = remainingAmount;
    }

    @Override
    public AccountType getType()
    {
        return AccountType.RealEstateAccount;
    }

    @Override
    public Result<Error> sendMoney(double amount, Iban target)
    {
        return Result.error(Error.NotSupported);
    }


    public Result<Error> calculateMonthlyRate()
    {
        monthlyAmount = (creditAmount * (repaymentRate + interestRate)) / 12;
        return Result.success();
    }


    @Override
    public Result<Error> receiveMoney(double amount)
    {
        try (var guard = new Guard(readLock()))
        {
            maxSpecialRepayment -= amount;
        }

        return super.receiveMoney(amount);
    }

    @Override
    public Result<Error> canTransfer(IAccount sourceAccount, double amount) {

        try (var guard = new Guard(readLock()))
        {
            if (getBalance() + amount > 0)
            {
                return Result.error(Error.BadAmount);
            }

            // special repayment above allowed limit ?
            if (amount > maxSpecialRepayment)
            {
                return Result.error(Error.LimitExceeded);
            }

            // special repayment only from giro account
            if (!(sourceAccount instanceof AccountGiro))
            {
                return Result.error(Error.BadSource);
            }

            // special repayment already used in this calendar year ?
            if (maxSpecialRepayment < creditAmount * 0.05)
            {
                return Result.error(Error.LimitExceeded);
            }

        }

        return Result.success();

    }
}
