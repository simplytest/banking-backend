package com.simplytest.server.api;

import java.util.HashMap;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.ValidationException;
import org.iban4j.IbanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.core.accounts.IAccount;
import com.simplytest.server.auth.JWT;
import com.simplytest.server.apiData.SendMoney;
import com.simplytest.server.apiData.TransferMoney;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import com.simplytest.server.utils.Result;
import com.simplytest.server.utils.Updatable;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping(path = "api/accounts")
@SecurityRequirement(name = "JwtAuth")
public class AccountController
{
    @Autowired
    private ContractRepository repository;

    private Updatable<IAccount> getAccount(Id id)
    {
        var parent = repository.findById(id.parent());

        if (parent.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var contract = parent.get().value();
        var account = contract.getAccount(id);

        if (!account.successful())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return Updatable.of(account.value(), () -> {
            repository.save(new DBContract(contract));
        });
    }

    private Updatable<HashMap<Id, IAccount>> getAccounts(Id id)
    {
        var parent = repository.findById(id.parent());

        if (parent.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var contract = parent.get().value();
        var account = contract.getAccounts();

        return Updatable.of(account, () -> {
            repository.save(new DBContract(contract));
        });
    }

    @ResponseBody
    @GetMapping(path = "{accountId}/balance")
    public Result<Double, Error> getCurrentBalance(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable @Valid long accountId, HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Result.error(Error.BadCredentials);
        }

        var id = new Id(parsedToken.get(), accountId);
        var account = getAccount(id).value();

        return Result.success(account.getBalance());
    }

    @ResponseBody
    @GetMapping(path = "{accountId}/receive")
    public Result<Boolean, Error> receiveMoney(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable @Valid long accountId, @RequestParam double amount,
            HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Result.error(Error.BadCredentials);
        }

        var id = new Id(parsedToken.get(), accountId);

        try (var updatable = getAccount(id))
        {
            var account = updatable.value();
            var result = account.receiveMoney(amount);

            if (!result.successful())
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return Result.error(result.error());
            }

            return Result.success();
        }
    }

    @ResponseBody
    @PostMapping(path = "{accountId}/send")
    public Result<Boolean, Error> sendMoney(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable @Valid long accountId, @RequestBody SendMoney data,
            HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Result.error(Error.BadCredentials);
        }

        var id = new Id(parsedToken.get(), accountId);

        try
        {
            IbanUtil.validate(data.target().raw());
        } catch (Exception e)
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return Result.error(Error.BadIban);
        }

        try (var updatable = getAccount(id))
        {
            var account = updatable.value();
            var result = account.sendMoney(data.amount(), data.target().value());

            if (!result.successful())
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return Result.error(result.error());
            }

            return Result.success();
        }
    }

    @Value("${validationurl}")
    String validationurl;

    @ResponseBody
    @PostMapping(path = "{accountId}/sendexternal")
    public Result<Boolean, Error> sendMoneyExternal(
        @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
        @PathVariable @Valid long accountId, @RequestBody SendMoney data,
        HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Result.error(Error.BadCredentials);
        }

        var id = new Id(parsedToken.get(), accountId);

        try
        {
            String validationUrl = this.validationurl + "/validator/validate?iban=" + data.target().raw();
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> validationResponse = restTemplate.getForEntity(validationUrl, String.class);

            if (!validationResponse.getStatusCode().is2xxSuccessful()) {
                throw new ValidationException("Fehler beim Validieren der IBAN");
            }

        } catch (Exception e)
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return Result.error(Error.BadIban);


        }

        try (var updatable = getAccount(id))
        {
            var account = updatable.value();
            var result = account.sendMoney(data.amount(), data.target().value());

            if (!result.successful())
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return Result.error(result.error());
            }

            return Result.success();
        }
    }


    @ResponseBody
    @PostMapping(path = "{accountId}/transfer")
    public Result<Boolean, Error> transferMoney(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @PathVariable @Valid long accountId,
            @RequestBody @Valid TransferMoney data, HttpServletResponse response)
    {
        var parsedToken = JWT.getId(token);

        if (parsedToken.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Result.error(Error.BadCredentials);
        }

        var id = new Id(parsedToken.get(), accountId);

        if (id.parent() != data.target().value().parent())
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return Result.error(Error.BadTarget);
        }

        try (var updatable = getAccounts(id))
        {
            var accounts = updatable.value();

            var source = accounts.get(id);
            var target = accounts.get(data.target().value());

            if (source == null || target == null)
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return Result.error(Error.BadTarget);
            }

            var result = source.transferMoney(data.amount(), target);

            if (!result.successful())
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return Result.error(result.error());
            }

            return Result.success();
        }
    }
}
