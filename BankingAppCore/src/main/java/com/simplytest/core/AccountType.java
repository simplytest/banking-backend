package com.simplytest.core;

public enum AccountType
{
    GiroAccount, FixedRateAccount, OnCallAccount, RealEstateAccount;

    public static AccountType getType(String type)
    {
        switch (type)
        {
            case "Giro Konto":
                return AccountType.GiroAccount;
            case "Tagesgeld Konto":
                return AccountType.OnCallAccount;
            case "Festgeld Konto":
                return AccountType.FixedRateAccount;
        }

        throw new UnsupportedOperationException();
    }
}