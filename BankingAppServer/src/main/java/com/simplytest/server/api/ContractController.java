package com.simplytest.server.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.simplytest.core.AccountType;
import com.simplytest.core.Contract;
import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.core.accounts.IAccount;
import com.simplytest.core.customers.Customer;
import com.simplytest.core.customers.CustomerBusiness;
import com.simplytest.core.customers.CustomerPrivate;
import com.simplytest.core.utils.Pair;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.data.CustomerData;
import com.simplytest.server.data.ContractResult;
import com.simplytest.server.data.RealEstateAccount;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import com.simplytest.server.utils.Updatable;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping(path = "api/contracts")
public class ContractController
{
    @Autowired
    private ContractRepository repository;

    private Updatable<Contract> findContract(long id)
    {
        var entry = repository.findById(id);

        if (entry.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var contract = entry.get().value();

        return Updatable.of(contract, () -> {
            repository.save(new DBContract(contract));
        });
    }

    @ResponseBody
    @PostMapping(path = "login/{id}")
    public Result<String, Error> login(@PathVariable long id,
            @RequestBody String password)
    {
        var contract = findContract(id);

        if (!contract.value().authenticate(password))
        {
            return Result.error(Error.BadCredentials);
        }

        return Result.success(JWT.generate(contract.value().getId()));
    }

    @ResponseBody
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public Result<ContractResult, Error> registerContract(
            @RequestBody @Valid CustomerData data,
            @RequestParam(required = false) Double initialBalance)
    {
        Customer customer;

        if (data.type == CustomerData.CustomerType.Private)
        {
            customer = new CustomerPrivate(data.firstName, data.lastName,
                    data.birthDay);
        } else
        {
            customer = new CustomerBusiness(data.firstName, data.lastName,
                    data.companyName, data.ustNumber);
        }

        customer.changeAddress(data.address);

        var dbEntry = repository.save(new DBContract());

        var contract = Contract.create(new Id(dbEntry.id()), customer,
                data.password);

        if (!contract.successful())
        {
            repository.delete(dbEntry);
            return Result.error(contract.error());
        }

        if (initialBalance != null)
        {
            for (var account : contract.value().getAccounts().values())
            {
                account.setBalance(initialBalance);
            }
        }

        dbEntry.setContract(contract.value());
        repository.save(dbEntry);

        var result = new ContractResult(dbEntry.id(), JWT.generate(dbEntry.id()));
        return Result.success(result);
    }

    @ResponseBody
    @GetMapping()
    public Contract getContract(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token)
    {
        var id = JWT.getId(token);
        return findContract(id).value();
    }

    @ResponseBody
    @DeleteMapping()
    public Result<Boolean, Error> dismissContract(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token)
    {
        var id = JWT.getId(token);

        var contract = findContract(id).value();
        var result = contract.dismiss();

        if (!result.successful())
        {
            return Result.error(result.error());
        }

        repository.deleteById(id);
        return Result.success();
    }

    @PutMapping(path = "dispo/{amount}")
    @ResponseBody
    public Result<Boolean, Error> changeDispoLimit(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable(required = true) Double amount)
    {
        var id = JWT.getId(token);

        try (var contract = findContract(id))
        {
            var result = contract.value().requestDispo(amount);

            if (!result.successful())
            {
                return Result.error(result.error());
            }

            return Result.success();
        }
    }

    @PutMapping(path = "sendLimit/{amount}")
    @ResponseBody
    public Result<Boolean, Error> changeSendLimit(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable(required = true) Double amount)
    {
        var id = JWT.getId(token);

        try (var contract = findContract(id))
        {
            var result = contract.value().setSendLimit(amount);

            if (!result.successful())
            {
                return Result.error(result.error());
            }

            return Result.success();
        }
    }

    @ResponseBody
    @GetMapping(path = "accounts")
    public Map<Id, IAccount> getContractAccounts(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token)
    {
        var id = JWT.getId(token);

        var contract = findContract(id).value();
        return contract.getAccounts();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "accounts/{type}")
    public Pair<Id, IAccount> addAccount(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable AccountType type)
    {
        var id = JWT.getId(token);

        try (var contract = findContract(id))
        {
            return contract.value().openAccount(type);
        }
    }

    @ResponseBody
    @PostMapping(path = "accounts")
    public Pair<Id, IAccount> addRealEstateAccount(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody @Valid RealEstateAccount data)
    {
        var id = JWT.getId(token);

        try (var contract = findContract(id))
        {
            return contract.value().openReaEstateAccount(data.repaymentRate(),
                    data.amount());
        }
    }

    @DeleteMapping(path = "accounts/{accountId}")
    @ResponseBody
    public Result<Boolean, Error> closeAccount(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable @Valid long accountId)
    {
        var id = JWT.getId(token);

        try (var contract = findContract(id))
        {
            var result = contract.value().closeAccount(new Id(id, accountId));

            if (!result.successful())
            {
                return Result.error(result.error());
            }

            return Result.success();
        }
    }

    @GetMapping(path = "customer")
    @ResponseBody
    public Customer getContractCustomer(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token)
    {
        var id = JWT.getId(token);

        var contract = findContract(id).value();
        return contract.getCustomer();
    }
}
