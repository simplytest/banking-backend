package com.simplytest.server.data;

public record SendMoney(Iban target, double amount)
{
}
