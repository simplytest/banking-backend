package com.simplytest.server.api;

import com.simplytest.core.customers.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.simplytest.core.customers.Customer;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Updatable;

import jakarta.servlet.http.HttpServletResponse;

@Validated
@RestController
@RequestMapping(path = "api/customers")
public class CustomerController
{
    @Autowired
    private ContractRepository repository;

    private Updatable<Customer> findCustomer(long accountId)
    {
        var parent = repository.findById(accountId);

        if (parent.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var contract = parent.get().value();

        return Updatable.of(contract.getCustomer(), () -> {
            repository.save(new DBContract(contract));
        });
    }

    @GetMapping()
    public Customer getCustomer(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return findCustomer(parsedToken.get()).value();
    }

    @PutMapping(path = "changeAddress")
    public void changeCustomerAddress(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody Address newAddress, HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try (var customer = findCustomer(parsedToken.get()))
        {
            customer.value().changeAddress(newAddress);
        }
    }
}
