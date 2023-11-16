package com.simplytest.core.accounts;

import com.simplytest.core.Error;
import com.simplytest.core.utils.Result;

import org.iban4j.Iban;

public class AccountRealEstate extends Account
{
    private double remainingAmount;
    private double clearanceRate;
    private double runtimeAmount;
    private double repaymentRate;
    private double creditAmount;
    private double payedAmount;
    private double monthlyRate;

    public AccountRealEstate(double repaymentRate)
    {
        this.repaymentRate = repaymentRate;
    }

    public double getMonthlyRate()
    {
        return monthlyRate;
    }

    public double getPayedAmount()
    {
        return payedAmount;
    }

    public double getCreditAmount()
    {
        return creditAmount;
    }

    public double getClearanceRate()
    {
        return clearanceRate;
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

    public void setMonthlyRate(double monthlyRate)
    {
        this.monthlyRate = monthlyRate;
    }

    public void setCreditAmount(double creditAmount)
    {
        this.creditAmount = creditAmount;
    }

    public void setClearanceRate(double clearanceRate)
    {
        this.clearanceRate = clearanceRate;
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

    public Result<Error> calculateMonthlyRate(double amount)
    {
        monthlyRate = (creditAmount + repaymentRate + interestRate) / 12;
        return Result.success();
    }
}
