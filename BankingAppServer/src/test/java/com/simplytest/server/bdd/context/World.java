package com.simplytest.server.bdd.context;

import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.utils.Result;

public class World
{
    public Result<?, Error> lastResult;

    public ContractRegistrationResult contract;
    public CustomerData customer;
    public Id account;

    public Exception lastError;
}
