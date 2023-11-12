package com.simplytest.server.data;

import jakarta.validation.constraints.NotNull;

public record RealEstateAccount(@NotNull double repaymentRate,
        @NotNull double amount)
{
}
