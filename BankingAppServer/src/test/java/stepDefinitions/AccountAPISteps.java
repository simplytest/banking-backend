package stepDefinitions;

import java.util.Optional;

import com.simplytest.core.Contract;
import com.simplytest.server.data.*;
import com.simplytest.server.utils.APIUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpMethod;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.core.utils.Pair;
import com.simplytest.server.utils.Result;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import testContext.World;

public class AccountAPISteps
{

    private final World world;
    private final ContractAPISteps contractAPISteps;

    public AccountAPISteps(World world, ContractAPISteps contractAPISteps)
    {
        this.world = world;
        this.contractAPISteps = contractAPISteps;
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
    public void ich_bin_registrierter_privatkunde_mit_konto_von_typ_mit_aktuellem_kontostand(
            String accountType, Integer balance)
    {
        contractAPISteps.ich_bin_ein_registrierter_privatkunde((double) balance);
        world.account = createAccount(world.contract, accountType);
    }

    @When("Ich den aktuellen Kontostand von {string} abfrage")
    public void ich_den_aktuellen_kontostand_abfrage(String type)
    {
        world.lastResult = new  Result<Double, Error>(Optional.of(APIUtil.<Double> request(
                String.format("accounts/%d/balance", world.account.child()),
                world.contract.JWT(), HttpMethod.GET, null, TypeToken.get(double.class))), null);

//        world.lastResult = APIUtil.<Result<Double, Error>> request(
//                String.format("accounts/%d/balance", world.account.child()),
//                world.contract.JWT(), HttpMethod.GET, null, TypeToken
//                        .getParameterized(Result.class, Double.class, Error.class));
    }

    @Then("beträgt der aktuelle Kontostand von {string} {int} €")
    public void betraegt_der_aktuelle_kontostand_von(String type, Integer amount)
    {
        ich_den_aktuellen_kontostand_abfrage(type);
        Assertions.assertEquals(world.lastResult.value(), (double) amount);
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
        var target = new AccountId(createAccount(world.contract, "OnCall").toString());

        world.lastResult = APIUtil.<Result<Boolean, Error>> request(
                String.format("accounts/%d/transfer", world.account.child(), amount),
                world.contract.JWT(), HttpMethod.POST, new TransferMoney(target, amount),
                TypeToken.getParameterized(Result.class, Boolean.class,
                        Error.class));
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



    @When("Ich einen neuen Real Estate Account erstelle")
    public void ich_einen_neues_real_estate_erstelle()
    {
        APIUtil.<Pair<Id, Object>> request("contracts/accounts", world.contract.JWT(),
                HttpMethod.POST, new RealEstateAccount(100, 100),
                TypeToken.getParameterized(Pair.class, Id.class, Object.class));

    }






    @Then("erhalte ich ein Konto von Typ {string}")
    public void erhalte_ich_ein_konto_von_typ(String type)
    {
        // todo: fix deserialization
        var data = APIUtil.<Contract> request("contracts", world.contract.JWT(), HttpMethod.GET,
                null, TypeToken.getParameterized(Contract.class));

        // todo: über kv iterieren und id abgreifen
        var account = data.getAccounts().values().stream().filter( a -> a.getType().name().equals(type)).findFirst();
        Assert.assertTrue("Missing account", account.isPresent());

//        var balance = APIUtil.<Double> request(
//                String.format("accounts/%d/balance", world.account.child()),
//                world.contract.JWT(), HttpMethod.GET, null, TypeToken.get(double.class));
//        Assertions.assertTrue(
//                data.getBody().contains(type.replaceAll("Konto", "").trim()));
    }

    @Then("erhalte ich ein Konto von Typ {string} dazu")
    public void erhalte_ich_ein_konto_von_typ_dazu(String type)
    {
        var data = APIUtil.request("contracts", world.contract.JWT(), HttpMethod.GET,
                null);

        Assertions.assertTrue(data.getBody().contains("00001"));
        Assertions.assertTrue(data.getBody().contains("00002"));

        Assertions.assertTrue(
                data.getBody().contains(type.replaceAll("Konto", "").trim()));
    }
}
