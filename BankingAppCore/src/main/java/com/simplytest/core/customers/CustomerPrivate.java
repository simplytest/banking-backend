package com.simplytest.core.customers;

import java.util.Date;

public class CustomerPrivate extends Customer
{
    private Date birthDay;
    private double schufaScore;

    public CustomerPrivate(String firstName, String lastName, Date birthDay)
    {
        super(firstName, lastName);

        this.birthDay = birthDay;
        this.setMonthlyFee(2.99);
        this.setTransactionFee(0.00);
    }

    public Date getBirthDay()
    {
        return birthDay;
    }

    public double getSchufaScore()
    {
        return schufaScore;
    }

    public void setBirthDay(Date birthDay)
    {
        this.birthDay = birthDay;
    }

    public void setSchufaScore(double schufaScore)
    {
        this.schufaScore = schufaScore;
    }

    @Override
    public boolean checkCredability(double amount)
    {
        if (schufaScore < 5.0)
        {
            return false;
        }

        if (schufaScore < 8.0 && amount > 2000)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null || other.getClass() != getClass())
        {
            return false;
        }

        var customer = (CustomerPrivate) other;

        return (customer.getFirstName().equals(getFirstName())
                && customer.getLastName().equals(getLastName())
                && customer.getBirthDay().equals(birthDay));
    }
}
