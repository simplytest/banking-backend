package com.simplytest.server.apiData;

import com.simplytest.core.Id;
import com.simplytest.server.validator.ValidAccountId;

@ValidAccountId
public class AccountId
{
    private String id;

    public AccountId(String id)
    {
        this.id = id;
    }

    public String raw()
    {
        return id;
    }

    public Id value()
    {
        return Id.from(id).value();
    }
}
