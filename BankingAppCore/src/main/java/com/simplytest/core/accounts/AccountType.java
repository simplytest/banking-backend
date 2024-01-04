package com.simplytest.core.accounts;

import com.simplytest.core.Error;

public enum AccountType
{
    GiroAccount,
    FixedRateAccount,
    OnCallAccount,
    RealEstateAccount;

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
            case "Immobilien-Finanzierungskonto":
                return AccountType.RealEstateAccount;
        }

        throw new UnsupportedOperationException();
    }

}
