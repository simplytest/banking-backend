package com.simplytest.core.accounts;

import org.iban4j.Iban;

import com.simplytest.core.Error;
import com.simplytest.core.utils.Result;

public interface IAccount
{
    public double getBalance();

    public AccountType getType();

    public double getBoundPeriod();

    public double getInterestRate();

    public double calculateInterest();

    public void setBalance(double balance);

    public void setBoundPeriod(double boundPeriod);

    public void setInterestRate(double interestRate);

    public Result<Error> receiveMoney(double amount);

    public Result<Error> sendMoney(double amount, Iban target);

    public Result<Error> transferMoney(double amount, IAccount targetAccount);

    public Result<Error> canTransfer(IAccount sourceAccount, double amount);
}
