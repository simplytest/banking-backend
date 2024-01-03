package com.simplytest.server;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mock.web.MockHttpServletResponse;

import com.simplytest.core.accounts.AccountType;
import com.simplytest.core.customers.Address;
import com.simplytest.server.api.ContractController;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.apiData.CustomerData.CustomerType;

@SpringBootApplication
public class BankingServer
{
    @Autowired
    private ContractController controller;

    public static CustomerData createDemoUser(String name, String password)
    {
        var address = new Address();

        address.setCity("City");
        address.setCountry("Country");
        address.setEmail("email@demo.com");
        address.setHouse("House");
        address.setStreet("Street");
        address.setZipCode("12345");

        var birthDay = Calendar.getInstance();
        birthDay.set(2000, 01, 01);

        var rtn = new CustomerData();

        rtn.address = address;
        rtn.type = CustomerType.Private;
        rtn.birthDay = birthDay.getTime();

        rtn.lastName = name;
        rtn.firstName = name;
        rtn.password = password;

        return rtn;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDemo()
    { /*
        if (System.getenv("SIMPLYTEST_DEMO") == null)
        {
            return;
        } */

        var response = new MockHttpServletResponse();

        controller.registerContract(createDemoUser("demo", "demo"), 1000.0,
                response);

        var john = createDemoUser("John", "123");
        var result = controller.registerContract(john, 0.0, response);
        controller.addAccount(result.value().JWT(), AccountType.FixedRateAccount);

        controller.registerContract(createDemoUser("Amanda", "123"), 0.0, response);
    }

    public static void main(String[] args)
    {
        SpringApplication.run(BankingServer.class, args);
    }
}
