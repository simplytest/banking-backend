package com.simplytest.core.customers;

import com.simplytest.core.Address;

public abstract class Customer
{
    private double transactionFee;
    private double monthlyFee;
    private String firstName;
    private String lastName;
    private Address address;

    public Customer(String firstName, String lastName)
    {
        this.address = new Address();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public Address getAddress()
    {
        return address;
    }

    public double getMonthlyFee()
    {
        return monthlyFee;
    }

    public double getTransactionFee()
    {
        return transactionFee;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public void setMonthlyFee(double monthlyFee)
    {
        this.monthlyFee = monthlyFee;
    }

    public void changeAddress(Address newAddress)
    {
        this.address = newAddress;
    }

    public void setTransactionFee(double transactionFee)
    {
        this.transactionFee = transactionFee;
    }

    public abstract boolean equals(Object o);

    public abstract boolean checkCredability(double amount);
}
