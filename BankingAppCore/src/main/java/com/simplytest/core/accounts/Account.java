package com.simplytest.core.accounts;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.simplytest.core.Error;
import com.simplytest.core.utils.Guard;
import com.simplytest.core.utils.Result;

public abstract class Account implements IAccount
{
    private transient ReadWriteLock mutex;
    private transient Lock writeLock;
    private transient Lock readLock;

    protected double balance;
    protected double boundPeriod;
    protected double interestRate;

    public Account()
    {
        this.mutex = new ReentrantReadWriteLock();
        this.writeLock = mutex.writeLock();
        this.readLock = mutex.readLock();
    }

    public double getBalance()
    {
        try (var guard = new Guard(readLock))
        {
            return balance;
        }
    }

    public double getBoundPeriod()
    {
        return boundPeriod;
    }

    public double getInterestRate()
    {
        return interestRate;
    }

    public double calculateInterest()
    {
        try (var guard = new Guard(readLock))
        {
            return balance * interestRate;
        }
    }

    public void setBalance(double balance)
    {
        try (var guard = new Guard(writeLock))
        {
            this.balance = balance;
        }
    }

    public void setBoundPeriod(double boundPeriod)
    {
        this.boundPeriod = boundPeriod;
    }

    public void setInterestRate(double interestRate)
    {
        this.interestRate = interestRate;
    }

    public Result<Error> receiveMoney(double amount)
    {
        if (amount <= 0)
        {
            return Result.error(Error.BadAmount);
        }

        try (var guard = new Guard(writeLock))
        {
            balance += amount;
        }

        return Result.success();
    }

    public Result<Error> transferMoney(double amount, IAccount target)
    {
        if (boundPeriod > 0 || target.getBoundPeriod() > 0)
        {
            return Result.error(Error.DisallowedDuringBound);
        }

        try (var guard = new Guard(writeLock))
        {
            if (amount > balance)
            {
                return Result.error(Error.BadBalance);
            }

            balance -= amount;
        }

        return target.receiveMoney(amount);
    }

    protected Lock readLock()
    {
        return this.readLock;
    }

    protected Lock writeLock()
    {
        return this.writeLock;
    }
}
