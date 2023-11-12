package com.simplytest.server.json;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.simplytest.core.accounts.Account;
import com.simplytest.core.accounts.IAccount;
import com.simplytest.core.customers.Customer;

public class Json
{
    private static Gson gson;

    public static GsonBuilder config()
    {
        var builder = new GsonBuilder();

        builder.setPrettyPrinting();

        builder.registerTypeAdapterFactory(DateTypeAdapter.FACTORY);

        builder.registerTypeAdapter(Optional.class, new OptionalAdapter<>());
        builder.registerTypeAdapter(Account.class, new AbstractAdapter<Account>());
        builder.registerTypeAdapter(Customer.class, new AbstractAdapter<Customer>());
        builder.registerTypeAdapter(IAccount.class, new AbstractAdapter<IAccount>());

        return builder;
    }

    public static Gson get()
    {
        if (gson == null)
        {
            var config = config().enableComplexMapKeySerialization();
            gson = config.create();
        }

        return gson;
    }
}
