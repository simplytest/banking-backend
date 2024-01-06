package com.simplytest.server.bdd.steps;

import org.springframework.http.HttpMethod;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.core.accounts.AccountType;
import com.simplytest.core.contracts.Contract;
import com.simplytest.core.utils.Pair;
import com.simplytest.server.apiData.AccountId;
import com.simplytest.server.apiData.ContractRegistrationResult;
import com.simplytest.server.apiData.Iban;
import com.simplytest.server.apiData.RealEstateAccount;
import com.simplytest.server.apiData.SendMoney;
import com.simplytest.server.apiData.TransferMoney;
import com.simplytest.server.bdd.factory.TestFactory;
import com.simplytest.server.utils.APIUtil;
import com.simplytest.server.utils.Result;

import org.junit.jupiter.api.Assertions;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static com.simplytest.core.accounts.AccountType.getType;

public class AccountAPISteps extends TestFactory
{
    private final ContractAPISteps contractAPISteps;

    public AccountAPISteps()
    {
        super();
        this.contractAPISteps = new ContractAPISteps();
    }

    public static Id createAccount(ContractRegistrationResult contract, String type)
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
    public void ich_bin_registrierter_privatkunde_mit_konto_von_typ_mit_aktuellem_kontostand(
            String accountType, Integer balance)
    {
        contractAPISteps.ich_bin_ein_registrierter_privatkunde((double) balance);
        world.account = createAccount(world.contract, accountType);
    }

    @When("Ich den aktuellen Kontostand von {string} abfrage")
    public void ich_den_aktuellen_kontostand_abfrage(String type)
    {
        var data = APIUtil.<Contract> request("contracts", world.contract.JWT(),
                HttpMethod.GET, null, TypeToken.get(Contract.class));

        var account = data.getAccounts().entrySet().stream()
                .filter(x -> x.getValue().getType() == getType(type)).findFirst();

        Assertions.assertTrue(account.isPresent(), "Account exists");

        world.lastResult = APIUtil.<Result<Double, Error>> request(
                String.format("accounts/%d/balance", account.get().getKey().child()),
                world.contract.JWT(), HttpMethod.GET, null,
                TypeToken.getParameterized(Result.class, Double.class, Error.class));
    }

    @Then("beträgt der aktuelle Kontostand von {string} {int} €")
    public void betraegt_der_aktuelle_kontostand_von(String type, Integer amount)
    {
        ich_den_aktuellen_kontostand_abfrage(type);
        Assertions.assertEquals((double) amount, world.lastResult.value());
    }

    @When("Ich auf {string} {int} € von einem gültigen externen Konto empfange")
    public void ich_auf_von_einem_gueltigen_externen_konto_empfange(String type,
            Integer amount)
    {
        var account = createAccount(world.contract, type);

        world.lastResult = APIUtil.<Result<Boolean, Error>> request(
                String.format("accounts/%d/receive?amount=%d", account.child(),
                        amount),
                world.contract.JWT(), HttpMethod.GET, null, TypeToken
                        .getParameterized(Result.class, Boolean.class, Error.class));
    }

    @Then("die Transaktion war erfolgreich")
    public void die_transaktion_war_erfolgreich()
    {
        Assertions.assertTrue(world.lastResult.successful());
    }

    @When("Ich per API von {string} {int} € auf ein gültiges internes Konto überweise")
    public void ich_von_auf_ein_gueltiges_internes_konto_ueberweise(String type,
            Integer amount)
    {
        var target = new AccountId(
                createAccount(world.contract, "OnCall").toString());

        world.lastResult = APIUtil.<Result<Boolean, Error>> request(
                String.format("accounts/%d/transfer", world.account.child(), amount),
                world.contract.JWT(), HttpMethod.POST,
                new TransferMoney(target, amount), TypeToken
                        .getParameterized(Result.class, Boolean.class, Error.class));
    }

    @When("Ich per API von {string} {int} € auf ein gültiges externes Konto überweise")
    public void ich_von_auf_ein_gueltiges_externes_konto_ueberweise(String type,
            Integer amount)
    {
        var iban = new Iban("AL47212110090000000235698741");

        world.lastResult = APIUtil.<Result<Boolean, Error>> request(
                String.format("accounts/%d/send", world.account.child(), amount),
                world.contract.JWT(), HttpMethod.POST, new SendMoney(iban, amount),
                TypeToken.getParameterized(Result.class, Boolean.class,
                        Error.class));
    }


    @When("Ich per API von {string} {int} € auf ein {string} übertrage")
    public void ichPerAPIVonTransferAmount€AufEinÜbertrage(String sourceAccountType, Integer transferAmount, String targetAccountType) {

        var contract = APIUtil.<Contract> request("contracts", world.contract.JWT(),
                HttpMethod.GET, null, TypeToken.get(Contract.class));

        var sourceAccount = contract.getAccounts().entrySet().stream()
                .filter(x -> x.getValue().getType() == getType(sourceAccountType)).findFirst();
        Assertions.assertTrue(sourceAccount.isPresent(), "Account exists");

        var targetAccount = contract.getAccounts().entrySet().stream()
                .filter(x -> x.getValue().getType() == getType(targetAccountType)).findFirst();
        Assertions.assertTrue(targetAccount.isPresent(), "Account exists");

        try {
            world.lastResult = APIUtil.<Result<Boolean, Error>> request(
                    String.format("accounts/%d/transfer", sourceAccount.get().getKey().child()),
                    world.contract.JWT(), HttpMethod.POST,
                    new TransferMoney(new AccountId(targetAccount.get().getKey().toString()), transferAmount), TypeToken
                            .getParameterized(Result.class, Boolean.class, Error.class));
        }
        catch (Exception e1)
        {
            world.lastError = e1;
        }

    }


    @Then("Die Transaktion wirft einen Fehler {string}")
    public void die_transaktion_wirft_einen_fehler(String error)
    {
        Assertions.assertTrue(world.lastError.toString().contains("\"error\": \"" + error + "\""));
    }

    @When("Ich ein neues Immobilien-Finanzierungskonto mit Kredit von {int} € und Tilgung von {int} % erstelle")
    public void ich_einen_neues_real_estate_account_erstelle(Integer amount,
            Integer repaymentRate)
    {
        var data = APIUtil.<Pair<Id, Object>> request("contracts/accounts",
                world.contract.JWT(), HttpMethod.POST,
                new RealEstateAccount(repaymentRate, amount),
                TypeToken.getParameterized(Pair.class, Id.class, Object.class));

        world.account = data.first();
    }

    @Then("erhalte ich ein Konto von Typ {string}")
    public void erhalte_ich_ein_konto_von_typ(String type)
    {
        var data = APIUtil.<Contract> request("contracts", world.contract.JWT(),
                HttpMethod.GET, null, TypeToken.get(Contract.class));

        var account = data.getAccounts().entrySet().stream()
                .filter(x -> x.getValue().getType() == getType(type)).findFirst();

        Assertions.assertTrue(account.isPresent(), "Account exists");

        world.account = account.get().getKey();
    }

}
