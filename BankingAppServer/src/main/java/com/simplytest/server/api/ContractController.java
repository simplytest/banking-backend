package com.simplytest.server.api;

import java.util.Map;

import com.simplytest.core.accounts.AccountType;
import com.simplytest.core.contracts.Contract;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.core.accounts.IAccount;
import com.simplytest.core.customers.Customer;
import com.simplytest.core.customers.CustomerBusiness;
import com.simplytest.core.customers.CustomerPrivate;
import com.simplytest.core.utils.Pair;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.apiData.CustomerData;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.apiData.RealEstateAccount;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import com.simplytest.server.utils.Updatable;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping(path = "api/contracts")
@SecurityRequirement(name = "JwtAuth")
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

    @Operation(summary = "Login", description = "Authenticates a contract using the provided password and returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @ResponseBody
    @PostMapping(path = "login/{id}")
    public Result<String, Error> login(@PathVariable long id,
            @RequestBody String password, HttpServletResponse response)
    {
        var contract = findContract(id);

        if (!contract.value().authenticate(password))
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return Result.error(Error.BadCredentials);
        }

        return Result.success(JWT.generate(contract.value().getId()));
    }

    @Operation(summary = "Register Contract", description = "Registers a new contract with the provided customer data and optional initial balance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contract created", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @ResponseBody
    @PostMapping()
    public Result<ContractRegistrationResult, Error> registerContract(
            @RequestBody @Valid CustomerData data,
            @RequestParam(required = false) Double initialBalance,
            HttpServletResponse response)
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
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

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

        var result = new ContractRegistrationResult(dbEntry.id(),
                JWT.generate(dbEntry.id()));
        response.setStatus(HttpServletResponse.SC_CREATED);

        return Result.success(result);
    }

    @Operation(summary = "Get Contract", description = "Returns the contract associated with the provided JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = Contract.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @ResponseBody
    @GetMapping()
    public Contract getContract(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return findContract(parsedToken.get()).value();
    }

    @Operation(summary = "Dismiss Contract", description = "Dismisses the contract associated with the provided JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @ResponseBody
    @DeleteMapping()
    public Result<Boolean, Error> dismissContract(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Result.error(Error.BadCredentials);
        }

        var contract = findContract(parsedToken.get()).value();

        if (System.getenv("SIMPLYTEST_DEMO") != null
                && contract.getCustomer().getFirstName().equals("Demo"))
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Result.error(Error.NotSupported);
        }

        var result = contract.dismiss();

        if (!result.successful())
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return Result.error(result.error());
        }

        repository.deleteById(parsedToken.get());

        return Result.success();
    }

    @Operation(summary = "Change Dispo Limit", description = "Changes the dispo limit for the contract associated with the provided JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @ResponseBody
    @PutMapping(path = "dispo/{amount}")
    public Result<Boolean, Error> changeDispoLimit(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable(required = true) Double amount,
            HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Result.error(Error.BadCredentials);
        }

        try (var contract = findContract(parsedToken.get()))
        {
            var result = contract.value().requestDispo(amount);

            if (!result.successful())
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return Result.error(result.error());
            }

            return Result.success();
        }
    }

    @Operation(summary = "Change Send Limit", description = "Changes the send limit for the contract associated with the provided JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @ResponseBody
    @PutMapping(path = "sendLimit/{amount}")
    public Result<Boolean, Error> changeSendLimit(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable(required = true) Double amount,
            HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Result.error(Error.BadCredentials);
        }

        try (var contract = findContract(parsedToken.get()))
        {
            var result = contract.value().setSendLimit(amount);

            if (!result.successful())
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return Result.error(result.error());
            }

            return Result.success();
        }
    }

    @Operation(summary = "Get Contract Accounts", description = "Returns the accounts associated with the contract provided by the JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @ResponseBody
    @GetMapping(path = "accounts")
    public Map<Id, IAccount> getContractAccounts(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        var contract = findContract(parsedToken.get()).value();
        return contract.getAccounts();
    }

    @Operation(summary = "Add Account", description = "Adds a new account to the contract associated with the provided JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created", content = @Content(schema = @Schema(implementation = Pair.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "accounts/{type}")
    public Pair<Id, IAccount> addAccount(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable AccountType type, HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        try (var contract = findContract(parsedToken.get()))
        {
            return contract.value().openAccount(type);
        }
    }

    @Operation(summary = "Add Real Estate Account", description = "Adds a new real estate account to the contract associated with the provided JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created", content = @Content(schema = @Schema(implementation = Pair.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @ResponseBody
    @PostMapping(path = "accounts")
    public Pair<Id, IAccount> addRealEstateAccount(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody @Valid RealEstateAccount data, HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        try (var contract = findContract(parsedToken.get()))
        {
            return contract.value().openRealEstateAccount(data.repaymentRate(),
                    data.amount());
        }
    }

    @Operation(summary = "Close Account", description = "Closes the specified account associated with the provided JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @ResponseBody
    @DeleteMapping(path = "accounts/{accountId}")
    public Result<Boolean, Error> closeAccount(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable @Valid long accountId, HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Result.error(Error.BadCredentials);
        }

        try (var contract = findContract(parsedToken.get()))
        {
            var result = contract.value()
                    .closeAccount(new Id(parsedToken.get(), accountId));

            if (!result.successful())
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return Result.error(result.error());
            }
        }

        return Result.success();
    }

    @Operation(summary = "Get Contract Customer", description = "Returns the customer associated with the contract provided by the JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = Customer.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @ResponseBody
    @GetMapping(path = "customer")
    public Customer getContractCustomer(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        var contract = findContract(parsedToken.get()).value();
        return contract.getCustomer();
    }
}
