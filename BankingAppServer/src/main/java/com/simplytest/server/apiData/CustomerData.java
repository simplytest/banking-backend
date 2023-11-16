package com.simplytest.server.apiData;

import java.util.Date;

import com.simplytest.core.customers.Address;
import com.simplytest.server.validator.ValidCustomer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@ValidCustomer
public class CustomerData
{
    public enum CustomerType
    {
        Private, Business
    }

    @NotNull
    @NotEmpty
    public String firstName;

    @NotNull
    @NotEmpty
    public String password;

    @NotNull
    @NotEmpty
    public String lastName;

    @NotNull
    public Address address;

    @NotNull
    public CustomerType type;

    public Date birthDay;
    public String ustNumber;
    public String companyName;
}
