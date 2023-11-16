package com.simplytest.server.apiData;

import jakarta.validation.constraints.NotNull;

public record RealEstateAccount(@NotNull double repaymentRate,
        @NotNull double amount)
{
}
