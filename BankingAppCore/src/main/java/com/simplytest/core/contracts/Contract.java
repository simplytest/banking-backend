package com.simplytest.core.contracts;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.core.accounts.AccountFixedRate;
import com.simplytest.core.accounts.AccountGiro;
import com.simplytest.core.accounts.AccountOnCall;
import com.simplytest.core.accounts.AccountRealEstate;
import com.simplytest.core.accounts.AccountType;
import com.simplytest.core.accounts.IAccount;
import com.simplytest.core.customers.Customer;
import com.simplytest.core.customers.CustomerPrivate;
import com.simplytest.core.utils.Expected;
import com.simplytest.core.utils.Guard;
import com.simplytest.core.utils.Pair;
import com.simplytest.core.utils.Result;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class Contract
{
    private Id id;
    private Customer customer;
    private volatile String passwordHash;
    private HashMap<Id, IAccount> accounts = new HashMap<>();

    private transient Lock readLock;
    private transient Lock writeLock;
    private transient ReadWriteLock mutex;

    private Contract()
    {
        this.mutex = new ReentrantReadWriteLock();

        this.readLock = mutex.readLock();
        this.writeLock = mutex.writeLock();
    }

    private Contract(Id id, Customer customer, String passwordHash)
    {
        this();

        this.id = id;
        this.customer = customer;
        this.passwordHash = hash(passwordHash);
    }

    public Id getId()
    {
        return id.clone();
    }

    public Customer getCustomer()
    {
        return customer;
    }

    public HashMap<Id, IAccount> getAccounts()
    {
        try (var guard = new Guard(readLock))
        {
            return (HashMap<Id, IAccount>) accounts.clone();
        }
    }

    public Expected<IAccount, Error> getAccount(Id id)
    {
        IAccount rtn;

        try (var guard = new Guard(readLock))
        {
            rtn = accounts.get(id);
        }

        if (rtn == null)
        {
            return Expected.error(Error.NotFound);
        }

        return Expected.success(rtn);
    }

    private String hash(String string)
    {
        return BCrypt.withDefaults().hashToString(12, string.toCharArray());
    }

    public boolean authenticate(String password)
    {
        return BCrypt.verifyer().verify(password.toCharArray(),
                passwordHash).verified;
    }

    public Result<Error> dismiss()
    {
        try (var guard = new Guard(this.writeLock))
        {
            var accounts = this.accounts.entrySet();

            for (var items : accounts)
            {
                var account = items.getValue();

                if (account.getBoundPeriod() <= 0)
                {
                    continue;
                }

                return Result.error(Error.DisallowedDuringBound);
            }

            for (var account : accounts)
            {
                closeAccount(account.getKey());
            }

            accounts.clear();
        }

        return Result.success();
    }

    public Pair<Id, IAccount> openAccount(AccountType type)
    {
        IAccount rtn = null;

        switch (type)
        {
        case GiroAccount:
            rtn = new AccountGiro();
            break;
        case FixedRateAccount:
            rtn = new AccountFixedRate();
            break;
        case OnCallAccount:
            rtn = new AccountOnCall();
            break;
        default:
            throw new UnsupportedOperationException();
        }

        var id = this.id.create();

        try (var guard = new Guard(writeLock))
        {
            this.accounts.put(id, rtn);
        }

        return Pair.of(id, rtn);
    }

    public Pair<Id, IAccount> openRealEstateAccount(double repaymentRate,
            double creditAmount)
    {
        var rtn = new AccountRealEstate(repaymentRate, creditAmount);
        rtn.calculateMonthlyRate();

        var id = this.id.create();

        try (var guard = new Guard(writeLock))
        {
            this.accounts.put(id, rtn);
        }

        return Pair.of(id, rtn);
    }

    public Result<Error> closeAccount(Id id)
    {
        IAccount account;

        try (var guard = new Guard(readLock))
        {
            account = accounts.get(id);
        }

        if (account == null)
        {
            return Result.error(Error.NotFound);
        }

        if (account.getBoundPeriod() > 0)
        {
            return Result.error(Error.DisallowedDuringBound);
        }

        try (var guard = new Guard(writeLock))
        {
            this.accounts.remove(id);
        }

        return Result.success();
    }

    public Result<Error> requestDispo(double amount)
    {
        if (!customer.getAddress().getCountry().equals("Deutschland"))
        {
            return Result.error(Error.BadCountry);
        }

        if (!customer.checkCredability(amount))
        {
            return Result.error(Error.BadCredability);
        }

        try (var guard = new Guard(readLock))
        {
            for (var item : accounts.entrySet())
            {
                var account = item.getValue();

                if (account.getType() != AccountType.GiroAccount)
                {
                    continue;
                }

                ((AccountGiro) account).setDispoLimit(amount);
            }
        }

        return Result.success();
    }

    public Result<Error> setSendLimit(double sendLimit)
    {
        try (var guard = new Guard(readLock))
        {
            for (var item : accounts.entrySet())
            {
                var account = item.getValue();

                if (account.getType() != AccountType.GiroAccount)
                {
                    continue;
                }

                ((AccountGiro) account).setSendLimit(sendLimit);
            }
        }

        return Result.success();
    }

    public static Expected<Contract, Error> create(Id id, Customer customer,
            String passwordHash)
    {
        if (customer.getAddress() == null)
        {
            return Expected.error(Error.BadCustomer);
        }

        if (customer instanceof CustomerPrivate)
        {
            var pCustomer = (CustomerPrivate) customer;

            var birthDay = new GregorianCalendar();
            birthDay.setTime(pCustomer.getBirthDay());

            var current = new GregorianCalendar();
            current.setTime(new Date());

            var age = current.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);

            if (age < 16)
            {
                return Expected.error(Error.Underage);
            }
        }

        if (customer.getAddress().getCountry().equals("UK"))
        {
            return Expected.error(Error.BadCountry);
        }

        var rtn = new Contract(id, customer, passwordHash);
        rtn.openAccount(AccountType.GiroAccount);

        return Expected.success(rtn);
    }
}
