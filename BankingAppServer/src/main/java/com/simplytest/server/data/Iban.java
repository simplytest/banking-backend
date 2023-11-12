package com.simplytest.server.data;

import com.simplytest.server.validator.ValidIban;

@ValidIban
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
