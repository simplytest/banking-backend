package com.simplytest.server.test.java.stepDefinitions;

import java.util.Calendar;
import java.util.Optional;

import com.simplytest.server.data.*;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Address;
import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.core.utils.Pair;
import com.simplytest.server.data.CustomerData.CustomerType;
import com.simplytest.server.test.java.utils.APIUtil;
import com.simplytest.server.utils.Result;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps
{
    private Result<?, Error> lastResult;

    private Id account;
    private ContractResult contract;

    public static ContractResult createContract(CustomerType type,
            Optional<Double> initialBalance)
    {
        var data = new CustomerData();
        data.type = type;

        var birthDay = Calendar.getInstance();
        birthDay.set(2000, 01, 01);

        data.birthDay = birthDay.getTime();
        data.address = new Address();
        data.firstName = "Foo";
        data.lastName = "Bar";

        data.password = "password";

        data.address.setStreet("Some Street");
        data.address.setZipCode("12345");

        String endpoint;

        if (initialBalance.isEmpty())
        {
            endpoint = String.format("contracts");
        } else
        {
            endpoint = String.format("contracts?initialBalance=%f",
                    initialBalance.get());
        }

        var result = APIUtil.<Result<ContractResult, Error>> request(endpoint, "",
                HttpMethod.POST, data, TypeToken.getParameterized(Result.class,
                        ContractResult.class, Error.class));

        return result.value();
    }

    public static Id createAccount(ContractResult contract, String type)
    {
        if (type.contains("Giro"))
        {
            return new Id(contract.id(), 1);
        }

        var rtn = APIUtil
                .<Pair<Id, Object>> request(
                        String.format("contracts/accounts/%s",
                                type.replaceAll("Konto", "").trim()
                                        .concat("Account")),
                        contract.JWT(), HttpMethod.POST, null,
                        TypeToken.getParameterized(Pair.class, Id.class,
                                Object.class));

        return rtn.first();
    }

    @Given("Ich bin registrierter Privatkunde mit Konto von Typ {string} mit aktuellem Kontostand {int} €")
    public void ich_bin_registrierter_privatkunde_mit_konto_von_typ_mit_aktuellem_kontostand_€(
            String accountType, Integer balance)
    {
        contract = createContract(CustomerType.Private,
                Optional.of((double) balance));

        account = createAccount(contract, accountType);
    }

    @Then("beträgt der aktuelle Kontostand von {string} {int} €")
    public void beträgt_der_aktuelle_kontostand_von_€(String type, Integer amount)
    {
        if (account == null)
        {
            account = createAccount(contract, "Giro");
        }

        var balance = APIUtil.<Double> request(
                String.format("accounts/%d/balance", account.child()),
                contract.JWT(), HttpMethod.GET, null, TypeToken.get(double.class));

        Assertions.assertEquals(balance, (double) amount);
    }

    @When("Ich auf {string} {int} € von einem gültigen externen Konto empfange")
    public void ich_auf_€_von_einem_gültigen_externen_konto_empfange(String type,
            Integer amount)
    {
        var account = createAccount(contract, type);

        lastResult = APIUtil.<Result<Boolean, Error>> request(
                String.format("accounts/%d/receive?amount=%d", account.child(),
                        amount),
                contract.JWT(), HttpMethod.GET, null, TypeToken
                        .getParameterized(Result.class, Boolean.class, Error.class));
    }

    @Then("die Transaktion war erfolgreich")
    public void die_transaktion_war_erfolgreich()
    {
        Assertions.assertTrue(lastResult.successful());
    }

    @When("Ich per API von {string} {int} € auf ein gültiges internes Konto überweise")
    public void ich_von_€_auf_ein_gültiges_internes_konto_überweise(String type,
            Integer amount)
    {
        var target = new AccountId(createAccount(contract, "OnCall").toString());

        lastResult = APIUtil.<Result<Boolean, Error>> request(
                String.format("accounts/%d/transfer", account.child(), amount),
                contract.JWT(), HttpMethod.POST, new TransferMoney(target, amount),
                TypeToken.getParameterized(Result.class, Boolean.class,
                        Error.class));
    }

    @When("Ich per API von {string} {int} € auf ein gültiges externes Konto überweise")
    public void ich_von_€_auf_ein_gültiges_externes_konto_überweise(String type,
            Integer amount)
    {
        var iban = new Iban("AL47212110090000000235698741");

        lastResult = APIUtil.<Result<Boolean, Error>> request(
                String.format("accounts/%d/send", account.child(), amount),
                contract.JWT(), HttpMethod.POST, new SendMoney(iban, amount),
                TypeToken.getParameterized(Result.class, Boolean.class,
                        Error.class));
    }

    @Given("Ich bin ein registrierter Privatkunde")
    public void ich_bin_ein_registrierter_privatkunde()
    {
        contract = Steps.createContract(CustomerType.Private, Optional.of(500.0));
    }

    @When("Ich einen neuen Vertrag abschliesse")
    public void ich_einen_neuen_vertrag_abschliesse()
    {
        contract = Steps.createContract(CustomerType.Private, Optional.empty());
    }

    @Given("Ich bin ein Privatkunde")
    public void ich_bin_ein_privatkunde()
    {
        // Nothing to do.
    }

    @When("Ich einen neuen Real Estate Account erstelle")
    public void ich_einen_neues_real_estate_erstelle()
    {
        APIUtil.<Pair<Id, Object>> request("contracts/accounts", contract.JWT(),
                HttpMethod.POST, new RealEstateAccount(100, 100),
                TypeToken.getParameterized(Pair.class, Id.class, Object.class));

    }

    @When("Ich meinen Vertrag kündige")
    public void ich_meinen_vertrag_kündige()
    {
        lastResult = APIUtil.<Result<Boolean, Error>> request("contracts",
                contract.JWT(), HttpMethod.DELETE, null, TypeToken
                        .getParameterized(Result.class, Boolean.class, Error.class));
    }

    @Then("mein Vertrag wurde gelöscht")
    public void mein_vertrag_wurde_gelöscht()
    {
        Assertions.assertTrue(lastResult.successful());

        Assertions.assertThrows(HttpClientErrorException.class, () -> APIUtil
                .request("contracts", contract.JWT(), HttpMethod.GET, null));
    }

    @Then("erhalte ich eine gültige Vertrag-ID")
    public void erhalte_ich_eine_gültige_vertrag_id()
    {
        Assertions.assertTrue(contract.id() > 0);
    }

    @Then("erhalte ich ein Konto von Typ {string}")
    public void erhalte_ich_ein_konto_von_typ(String type)
    {
        var data = APIUtil.request("contracts", contract.JWT(), HttpMethod.GET,
                null);

        Assertions.assertTrue(
                data.getBody().contains(type.replaceAll("Konto", "").trim()));
    }

    @Then("erhalte ich ein Konto von Typ {string} dazu")
    public void erhalte_ich_ein_konto_von_typ_dazu(String type)
    {
        var data = APIUtil.request("contracts", contract.JWT(), HttpMethod.GET,
                null);

        Assertions.assertTrue(data.getBody().contains("00001"));
        Assertions.assertTrue(data.getBody().contains("00002"));

        Assertions.assertTrue(
                data.getBody().contains(type.replaceAll("Konto", "").trim()));
    }
}
