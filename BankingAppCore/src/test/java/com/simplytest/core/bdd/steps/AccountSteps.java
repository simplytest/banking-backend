package com.simplytest.core.bdd.steps;

import java.util.ArrayList;
import java.util.HashMap;

import org.iban4j.Iban;
import org.junit.jupiter.api.Assertions;

import com.simplytest.core.Error;
import com.simplytest.core.accounts.AccountFixedRate;
import com.simplytest.core.accounts.AccountGiro;
import com.simplytest.core.accounts.AccountOnCall;
import com.simplytest.core.accounts.AccountType;
import com.simplytest.core.accounts.IAccount;
import com.simplytest.core.bdd.mocks.ContractsDBMock;
import com.simplytest.core.contracts.Contract;
import com.simplytest.core.utils.Result;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AccountSteps
{
    private HashMap<AccountType, IAccount> accounts = new HashMap<AccountType, IAccount>();
    private Result<Error> lastResult;
    private Contract contract;

    private static final float delta = 0.0000001f;

    public static AccountType getType(String type)
    {
        switch (type)
        {
        case "Giro Konto":
            return AccountType.GiroAccount;
        case "Tagesgeld Konto":
            return AccountType.OnCallAccount;
        case "Festgeld Konto":
            return AccountType.FixedRateAccount;
        }

        throw new UnsupportedOperationException();
    }

    public static Error getError(String string)
    {
        switch (string)
        {
        case "":
            return Error.Unknown;
        case "Überweisung wegen Limitüberschreitung nicht möglich":
            return Error.LimitExceeded;
        case "Transfer wegen Unterdeckung nicht möglich":
        case "Überweisung wegen Unterdeckung nicht möglich":
            return Error.BadBalance;
        case "Transfer während der gebundener Laufzeit nicht möglich":
            return Error.DisallowedDuringBound;
        case "Unzureichende Kreditwürdigkeit":
            return Error.BadCredability;
        case "Dispovolumen für Kunden aus Ausland nicht unterstützt":
        case "Vertragsabschlusses für Kunden mit Wohnsitz aussesrhalb EU nicht möglich":
            return Error.BadCountry;
        case "Vertragsabschlusses für minderjährige Personen nicht möglich":
            return Error.Underage;
        case "Kunde bereits registriert":
            return Error.AlreadyRegistered;
        }

        throw new UnsupportedOperationException(string);
    }

    public IAccount getAccount(AccountType type)
    {
        if (!accounts.containsKey(type))
        {
            switch (type)
            {
            case GiroAccount:
                accounts.put(type, new AccountGiro());
                break;
            case OnCallAccount:
                accounts.put(type, new AccountOnCall());
                break;
            case FixedRateAccount:
                accounts.put(type, new AccountFixedRate());
                break;
            default:
                throw new UnsupportedOperationException(type.toString());
            }
        }

        return accounts.get(type);
    }

    @Given("Als Privatkunde habe ich ein Konto von Typ {string} mit aktuellem Kontostand {float} €")
    public void als_privatkunde_habe_ich_ein_konto_von_typ_mit_aktuellem_kontostand_€(
            String type, float balance)
    {
        var account = getAccount(getType(type));
        account.setBalance(balance);

        Assertions.assertEquals(account.getBalance(), balance, delta);
    }

    @When("Ich von {string} {float} € auf ein gültiges externes Konto überweise")
    public void ich_von_€_auf_ein_gültiges_externes_konto_überweise(String type,
            float amount)
    {
        var account = getAccount(getType(type));
        lastResult = account.sendMoney(amount, Iban.valueOf("AT483200000012345864"));
    }

    @Then("Ich erhalte eine {string} meiner/meines Überweisung/Transfers mit der Meldung {string}")
    public void ich_erhalte_eine_meiner_überweisung_mit_der_meldung(String type,
            String error)
    {
        var success = type.equals("Bestätigung");
        Assertions.assertEquals(lastResult.successful(), success);

        if (lastResult.successful())
        {
            return;
        }

        Assertions.assertEquals(lastResult.error(), getError(error));
    }

    @Then("der aktuelle Kontostand von {string} beträgt {float} €")
    public void der_aktuelle_kontostand_von_beträgt_€(String type, float amount)
    {
        var account = getAccount(getType(type));
        Assertions.assertEquals(account.getBalance(), amount, delta);
    }

    @When("Ich von {string} {float} € auf das {string} transferiere")
    public void ich_von_€_auf_das_transferiere(String type, float amount,
            String target)
    {
        var account = getAccount(getType(type));
        var other = getAccount(getType(target));

        lastResult = account.transferMoney(amount, other);
    }

    @Then("Ich erhalte ich eine Bestätigung des erfolgten Transfers")
    public void ich_erhalte_ich_eine_bestätigung_des_erfolgten_transfers()
    {
        Assertions.assertTrue(lastResult.successful());
    }

    @Given("verbleibender gebundener Laufzeit von {string} von {double} Jahren")
    public void verbleibender_gebundener_laufzeit_von_von_jahren(String type,
            Double remaining)
    {
        var account = getAccount(getType(type));
        account.setBoundPeriod(remaining);

        Assertions.assertEquals(account.getBoundPeriod(), remaining, delta);
    }

    @Given("mein Dispovolumen beträgt {float} €")
    public void mein_dispovolumen_beträgt_€(float limit)
    {
        var account = (AccountGiro) accounts.get(AccountType.GiroAccount);
        account.setDispoLimit(limit);

        Assertions.assertEquals(account.getDispoLimit(), limit, delta);
    }

    @Given("mein Überweisungslimit beträgt {float} €")
    public void mein_überweisungslimit_beträgt_€(float limit)
    {
        var account = (AccountGiro) accounts.get(AccountType.GiroAccount);
        account.setSendLimit(limit);

        Assertions.assertEquals(account.getSendLimit(), limit, delta);
    }

    @Given("Als Privatkunde habe ich einen Schufa Score von {double}")
    public void als_privatkunde_habe_ich_einen_schufa_score_von(Double score)
    {
        var customer = ContractSteps.dummyPrivate();

        customer.setSchufaScore(score);
        Assertions.assertEquals(customer.getSchufaScore(), score, delta);

        var contract = ContractsDBMock.createContract(customer);
        Assertions.assertTrue(contract.successful());

        this.contract = contract.value();
        var contractAccounts = new ArrayList<>(this.contract.getAccounts().values());

        accounts.put(AccountType.GiroAccount, contractAccounts.get(0));
    }

    @Given("Ich bin ein Privatkunde aus {string}")
    public void ich_bin_ein_privatkunde_aus(String country)
    {
        var customer = ContractSteps.dummyPrivate();

        customer.getAddress().setCountry(country);
        Assertions.assertEquals(customer.getAddress().getCountry(), country);

        var contract = ContractsDBMock.createContract(customer);
        Assertions.assertTrue(contract.successful());

        this.contract = contract.value();
        var contractAccounts = new ArrayList<>(this.contract.getAccounts().values());

        accounts.put(AccountType.GiroAccount, contractAccounts.get(0));
    }

    @Given("Als Geschäftskunde habe ich einen Jahresumsatz von {float}")
    public void als_geschäftskunde_habe_ich_einen_jahresumsatz_von(float amount)
    {
        ContractsDBMock.clear();
        var customer = ContractSteps.dummyBusiness();

        customer.setRevenue(amount);
        Assertions.assertEquals(customer.getRevenue(), amount, delta);

        var contract = ContractsDBMock.createContract(customer);
        Assertions.assertTrue(contract.successful());

        this.contract = contract.value();
        var contractAccounts = new ArrayList<>(this.contract.getAccounts().values());

        accounts.put(AccountType.GiroAccount, contractAccounts.get(0));
    }

    @When("Ich einen Antrag auf Dispovolumen von {float} € stelle")
    public void ich_einen_antrag_auf_dispovolumen_von_€_stelle(float amount)
    {
        lastResult = contract.requestDispo(amount);
    }

    @Then("Ich erhalte ich eine Ablehnung des Dispovolumens mit der Meldung {string}")
    public void ich_erhalte_ich_eine_ablehnung_des_dispovolumens_mit_der_meldung(
            String error)
    {
        Assertions.assertFalse(lastResult.successful());
        Assertions.assertEquals(lastResult.error(), getError(error));
    }

    @Then("Ich erhalte eine Bestätigung des erfolgten Transfers")
    public void ich_erhalte_eine_bestätigung_des_erfolgten_transfers()
    {
        Assertions.assertTrue(lastResult.successful());
    }

    @Then("Ich erhalte eine Bestätigung für die Gewährung des Dispovolumens")
    public void ich_erhalte_eine_bestätigung_für_die_gewährung_des_dispovolumens()
    {
        Assertions.assertTrue(lastResult.successful());
    }

    @Then("Ich erhalte eine Ablehnung des Dispovolumens mit der Meldung {string}")
    public void ich_erhalte_eine_ablehnung_des_dispovolumens_mit_der_meldung(
            String error)
    {
        Assertions.assertFalse(lastResult.successful());
        Assertions.assertEquals(lastResult.error(), getError(error));
    }
}
