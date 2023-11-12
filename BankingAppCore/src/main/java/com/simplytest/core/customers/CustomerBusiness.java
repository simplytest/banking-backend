package com.simplytest.core.customers;

public class CustomerBusiness extends Customer
{
    private String companyName;
    private String ustNumber;
    private double revenue;

    public CustomerBusiness(String firstName, String lastName, String companyName,
            String ustNumber)
    {
        super(firstName, lastName);

        this.ustNumber = ustNumber;
        this.companyName = companyName;

        this.setMonthlyFee(8.99);
        this.setTransactionFee(0.20);
    }

    public double getRevenue()
    {
        return revenue;
    }

    public String getUstNumber()
    {
        return ustNumber;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    public void setUstNumber(String ustNumber)
    {
        this.ustNumber = ustNumber;
    }

    public void setRevenue(double revenue)
    {
        this.revenue = revenue;
    }

    @Override
    public boolean checkCredability(double amount)
    {
        if (revenue < 300000)
        {
            return false;

        }

        if (revenue < 1000000 && amount > 2000)
        {
            return false;
        }

        return revenue / 200 > amount;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null || other.getClass() != getClass())
        {
            return false;
        }

        var customer = (CustomerBusiness) other;

        return customer.getCompanyName().equals(companyName)
                && customer.getUstNumber().equals(ustNumber);
    }
}
