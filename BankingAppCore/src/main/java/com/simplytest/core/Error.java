package com.simplytest.core;

public enum Error
{
    BadAmount,
    BadBalance,
    BadCustomer,
    BadCredability,
    BadCountry,
    DisallowedDuringBound,
    NotSupported,
    NotImplemented,
    LimitExceeded,
    Underage,
    Unknown,
    AlreadyRegistered,
    NotFound,
    BadSource,
    BadTarget,
    BadCredentials,
    BadIban;

    public static Error getError(String string)
    {
        switch (string)
        {
            case "":
                return Error.Unknown;
            case "Überweisung wegen Limitüberschreitung nicht möglich":
                return Error.LimitExceeded;
            case "Transfer wegen Unterdeckung nicht möglich":
            case "Überweisung wegen Unterdeckung nicht möglich":
                return Error.BadBalance;
            case "Transfer während der gebundener Laufzeit nicht möglich":
                return Error.DisallowedDuringBound;
            case "Unzureichende Kreditwürdigkeit":
                return Error.BadCredability;
            case "Dispovolumen für Kunden aus Ausland nicht unterstützt":
            case "Vertragsabschlusses für Kunden mit Wohnsitz aussesrhalb EU nicht möglich":
                return Error.BadCountry;
            case "Vertragsabschlusses für minderjährige Personen nicht möglich":
                return Error.Underage;
            case "Kunde bereits registriert":
                return Error.AlreadyRegistered;
            case "Transaktion von diesem Quellkonto nicht erlaubt":
                return Error.BadSource;
            case "Betrag nicht zulässig":
                return Error.BadAmount;
        }

        throw new UnsupportedOperationException(string);
    }

}
