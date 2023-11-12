package com.simplytest.server.api;

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

import com.simplytest.core.Address;
import com.simplytest.core.customers.Customer;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Updatable;

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
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token)
    {
        var id = JWT.getId(token);
        return findCustomer(id).value();
    }

    @PutMapping(path = "changeAddress")
    public void changeCustomerAddress(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody Address newAddress)
    {
        var id = JWT.getId(token);

        try (var customer = findCustomer(id))
        {
            customer.value().changeAddress(newAddress);
        }
    }
}
