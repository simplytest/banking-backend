package com.simplytest.server.apiData;

import jakarta.validation.Valid;

public record TransferMoney(@Valid AccountId target, double amount)
{
}
