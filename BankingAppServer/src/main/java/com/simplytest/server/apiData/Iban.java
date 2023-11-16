package com.simplytest.server.apiData;

public class Iban
{
    private String iban;

    public Iban(String iban)
    {
        this.iban = iban;
    }

    public String raw()
    {
        return iban;
    }

    public org.iban4j.Iban value()
    {
        return org.iban4j.Iban.valueOf(iban);
    }
}
