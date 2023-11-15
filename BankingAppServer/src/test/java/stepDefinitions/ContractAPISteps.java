package stepDefinitions;

import com.google.gson.reflect.TypeToken;
import com.simplytest.core.Address;
import com.simplytest.core.Error;
import com.simplytest.server.data.ContractResult;
import com.simplytest.server.data.CustomerData;
import com.simplytest.server.utils.APIUtil;
import com.simplytest.server.utils.Result;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import testContext.World;

import java.util.Calendar;
import java.util.Optional;

public class ContractAPISteps {

    private final World world;

    public ContractAPISteps(World world) {
        this.world = world;
    }

    @Given("Ich bin ein Privatkunde")
    public void ich_bin_ein_privatkunde()
    {
        world.customer = new CustomerData();
        world.customer.type = CustomerData.CustomerType.Private;

        var birthDay = Calendar.getInstance();
        birthDay.set(2000, 01, 01);

        world.customer.birthDay = birthDay.getTime();
        world.customer.address = new Address();
        world.customer.firstName = "Foo";
        world.customer.lastName = "Bar";

        world.customer.password = "password";

        world.customer.address.setStreet("Some Street");
        world.customer.address.setZipCode("12345");
    }

    @Given("Ich bin ein registrierter Privatkunde")
    public void ich_bin_ein_registrierter_privatkunde()
    {
        ich_bin_ein_privatkunde();
        world.contract = createContract(Optional.of(500.0));
    }

    @Given("Ich bin ein registrierter Privatkunde mit Kontostandt {double}")
    public void ich_bin_ein_registrierter_privatkunde(double initialBalance)
    {
        ich_bin_ein_privatkunde();
        world.contract = createContract(Optional.of(initialBalance));
    }

    @When("Ich einen neuen Vertrag abschliesse")
    public void ich_einen_neuen_vertrag_abschliesse()
    {
        world.contract = createContract(Optional.empty());
    }

    private ContractResult createContract(Optional<Double> initialBalance)
    {
        String endpoint;

        if (initialBalance.isEmpty())
        {
            endpoint = String.format("contracts");
        } else
        {
            endpoint = String.format("contracts?initialBalance=%s",
                    initialBalance.get());
        }

        var result = APIUtil.<Result<ContractResult, Error>> request(endpoint, "",
                HttpMethod.POST, world.customer, TypeToken.getParameterized(Result.class,
                        ContractResult.class, Error.class));

        return result.value();
    }

    @When("Ich meinen Vertrag kündige")
    public void ich_meinen_vertrag_kuendige()
    {
        world.lastResult = APIUtil.<Result<Boolean, Error>> request("contracts",
                world.contract.JWT(), HttpMethod.DELETE, null, TypeToken
                        .getParameterized(Result.class, Boolean.class, Error.class));
    }

    @Then("erhalte ich eine gültige Vertrag-ID")
    public void erhalte_ich_eine_gueltige_vertrag_id()
    {
        Assertions.assertTrue(world.contract.id() > 0);
    }

    @Then("mein Vertrag wurde entfernt")
    public void mein_vertrag_wurde_geloescht()
    {
        Assertions.assertTrue(world.lastResult.successful());

        Assertions.assertThrows(HttpClientErrorException.class, () -> APIUtil
                .request("contracts", world.contract.JWT(), HttpMethod.GET, null));
    }







}
