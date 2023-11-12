package com.simplytest.core.test.java.stepDefinitions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.jupiter.api.Assertions;

import com.simplytest.core.Contract;
import com.simplytest.core.Error;
import com.simplytest.core.customers.Customer;
import com.simplytest.core.customers.CustomerBusiness;
import com.simplytest.core.customers.CustomerPrivate;
import com.simplytest.core.test.java.mocks.ContractsDBMock;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ContractSteps
{
    private Customer customer;
    private Contract contract;
    private Error lastError;

    public static CustomerPrivate dummyPrivate()
    {
        var cal = Calendar.getInstance();
        cal.set(1969, 06, 30);

        var birthday = cal.getTime();
        var rtn = new CustomerPrivate("Max", "Mustermann", birthday);

        Assertions.assertNotNull(rtn.getBirthDay());

        return rtn;
    }

    public static CustomerBusiness dummyBusiness()
    {
        var company = "Company";
        var ust = "DE123456789";

        var rtn = new CustomerBusiness("Max", "Mustermann", company, ust);

        Assertions.assertEquals(rtn.getCompanyName(), company);
        Assertions.assertEquals(rtn.getUstNumber(), ust);

        return rtn;
    }

    @Given("Ich bin ein existierender Privatkunde mit Vorname {string} und Nachname {string} und Geburtsdatum {string}")
    public void ich_bin_ein_existierender_privatkunde_mit_vorname_und_nachname_und_geburtsdatum(
            String firstName, String lastName, String birthday) throws ParseException
    {
        customer = dummyPrivate();

        ich_als_vorname_eintrage(firstName);
        ich_als_name_eintrage(lastName);
        ich_als_geburtsdatum_eintrage(birthday);

        ContractsDBMock.createContract(customer);
    }

    @Given("Ich bin ein neuer Geschäftskunde")
    public void ich_bin_ein_neuer_geschäftskunde()
    {
        customer = dummyBusiness();
    }

    @Then("Ich erhalte eine gültige ID des erfolgten Vertragsabschlusses")
    public void ich_erhalte_eine_gültige_id_des_erfolgten_vertragsabschlusses()
    {
        Assertions.assertNotNull(contract.getId());
    }

    @Given("Ich bin ein existierender Geschäftskunde mit Firmenname {string} und Umsatzsteuernummer {string}")
    public void ich_bin_ein_existierender_geschäftskunde_mit_firmenname_und_umsatzsteuernummer(
            String company, String ust)
    {
        customer = dummyBusiness();

        ich_als_firmenname_eintrage(company);
        ich_als_umsatzsteuernummer_eintrage(ust);

        ContractsDBMock.createContract(customer);
    }

    @When("Ich als Firmenname {string} eintrage")
    public void ich_als_firmenname_eintrage(String name)
    {
        var customer = (CustomerBusiness) this.customer;
        customer.setCompanyName(name);

        Assertions.assertEquals(customer.getCompanyName(), name);
    }

    @When("Ich als Umsatzsteuernummer {string} eintrage")
    public void ich_als_umsatzsteuernummer_eintrage(String ust)
    {
        var customer = (CustomerBusiness) this.customer;
        customer.setUstNumber(ust);

        Assertions.assertEquals(customer.getUstNumber(), ust);
    }

    @When("Ich als Land {string} eintrage")
    public void ich_als_land_eintrage(String country)
    {
        customer.getAddress().setCountry(country);
        Assertions.assertEquals(customer.getAddress().getCountry(), country);
    }

    @Given("Ich bin ein neuer Privatkunde")
    public void ich_bin_ein_neuer_privatkunde()
    {
        customer = dummyPrivate();
    }

    @When("Ich als Vorname {string} eintrage")
    public void ich_als_vorname_eintrage(String name)
    {
        customer.setFirstName(name);
        Assertions.assertEquals(customer.getFirstName(), name);
    }

    @When("Ich als Name {string} eintrage")
    public void ich_als_name_eintrage(String name)
    {
        customer.setLastName(name);
        Assertions.assertEquals(customer.getLastName(), name);
    }

    @When("Ich als Geburtsdatum {string} eintrage")
    public void ich_als_geburtsdatum_eintrage(String string) throws ParseException
    {
        var date = new SimpleDateFormat("dd.MM.yyyy").parse(string);
        var customer = (CustomerPrivate) this.customer;

        customer.setBirthDay(date);
        Assertions.assertEquals(customer.getBirthDay(), date);
    }

    @When("Ich als Strasse {string} eintrage")
    public void ich_als_strasse_eintrage(String street)
    {
        customer.getAddress().setStreet(street);
        Assertions.assertEquals(customer.getAddress().getStreet(), street);
    }

    @When("Ich als Hausnummer {string} eintrage")
    public void ich_als_hausnummer_eintrage(String house)
    {
        customer.getAddress().setHouse(house);
        Assertions.assertEquals(customer.getAddress().getHouse(), house);
    }

    @When("Ich als Ort {string} eintrage")
    public void ich_als_ort_eintrage(String city)
    {
        customer.getAddress().setCity(city);
        Assertions.assertEquals(customer.getAddress().getCity(), city);
    }

    @When("Ich als PLZ {string} eintrage")
    public void ich_als_plz_eintrage(String code)
    {
        customer.getAddress().setZipCode(code);
        Assertions.assertEquals(customer.getAddress().getZipCode(), code);
    }

    @When("Ich als Mail-Addresse {string} eintrage")
    public void ich_als_mail_addresse_eintrage(String mail)
    {
        customer.getAddress().setEmail(mail);
        Assertions.assertEquals(customer.getAddress().getEmail(), mail);
    }

    @When("Vertragabschluss starte")
    public void vertragabschluss_starte()
    {
        var contract = ContractsDBMock.createContract(customer);

        if (!contract.successful())
        {
            lastError = contract.error();
            return;
        }

        this.contract = contract.value();
    }

    @Then("Ich erhalte eine Ablehnung des Vertragsabschlusses mit der Meldung {string}")
    public void ich_erhalte_eine_ablehnung_des_vertragsabschlusses_mit_der_meldung(
            String error)
    {
        Assertions.assertEquals(lastError, AccountSteps.getError(error));
    }
}
