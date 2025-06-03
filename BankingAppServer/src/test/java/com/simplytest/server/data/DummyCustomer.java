package com.simplytest.server.data;

import com.simplytest.core.customers.Address;
import com.simplytest.server.apiData.CustomerData;

import java.util.Calendar;

public class DummyCustomer {
    public CustomerData createDefaultCustomerDTO() {
        CustomerData customer = new CustomerData();
        Address address = new Address();
        address.setCity("Gotham");
        address.setCountry("Germany");
        address.setEmail("mey");
        address.setHouse("42");
        address.setStreet("Mr Hyde Str");
        address.setZipCode("878765");
        customer.address = address;
        customer.firstName = "hanibal";
        customer.lastName = ("lecter");
        customer.password = ("demo");

        var birthDay = Calendar.getInstance();
        birthDay.set(2000, 01, 01);
        customer.birthDay = birthDay.getTime();
        customer.ustNumber = ("35252342");
        customer.companyName = ("Imortal Inc.");
        customer.type = CustomerData.CustomerType.Private;
        return customer;
    }
    public CustomerData createCustomerDTO(String password) {
        CustomerData customer = new CustomerData();
        Address address = new Address();
        address.setCity("Gotham");
        address.setCountry("Germany");
        address.setEmail("mey");
        address.setHouse("42");
        address.setStreet("Mr Hyde Str");
        address.setZipCode("878765");
        customer.address = address;
        customer.firstName = "hanibal";
        customer.lastName = ("lecter");
        customer.password = password;

        var birthDay = Calendar.getInstance();
        birthDay.set(2000, 01, 01);
        customer.birthDay = birthDay.getTime();
        customer.ustNumber = ("35252342");
        customer.companyName = ("Imortal Inc.");
        customer.type = CustomerData.CustomerType.Private;
        return customer;
    }
}
