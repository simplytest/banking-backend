package testContext;

import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.server.data.ContractResult;
import com.simplytest.server.data.CustomerData;
import com.simplytest.server.utils.Result;

public class World {

    public Result<?, Error> lastResult;

    public Id account;
    public ContractResult contract;
    public CustomerData customer;

}
