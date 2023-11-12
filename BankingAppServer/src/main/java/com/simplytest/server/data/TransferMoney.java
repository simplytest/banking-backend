package com.simplytest.server.data;

import jakarta.validation.Valid;

public record TransferMoney(@Valid AccountId target, double amount)
{
}
