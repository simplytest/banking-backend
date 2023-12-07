package com.simplytest.server;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mock.web.MockHttpServletResponse;

import com.simplytest.core.customers.Address;
import com.simplytest.server.api.ContractController;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.apiData.CustomerData.CustomerType;

@SpringBootApplication
public class BankingServer
{
    @Autowired
    private ContractController controller;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDemo()
    {
        if (System.getenv("SIMPLYTEST_DEMO") != null)
        {
            return;
        }

        var data = new CustomerData();
        data.address = new Address();

        data.address.setCity("City");
        data.address.setCountry("Country");
        data.address.setEmail("email@demo.com");
        data.address.setHouse("House");
        data.address.setStreet("Street");
        data.address.setZipCode("12345");

        var birthDay = Calendar.getInstance();
        birthDay.set(2000, 01, 01);

        data.lastName = "Demo";
        data.firstName = "Demo";

        data.type = CustomerType.Private;
        data.birthDay = birthDay.getTime();

        data.password = "demo";

        var response = new MockHttpServletResponse();
        controller.registerContract(data, 1000.0, response);
    }

    public static void main(String[] args)
    {
        SpringApplication.run(BankingServer.class, args);
    }
}
