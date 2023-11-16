package mocks;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.simplytest.core.Error;
import com.simplytest.core.Id;
import com.simplytest.core.accounts.IAccount;
import com.simplytest.core.contracts.Contract;
import com.simplytest.core.customers.Customer;
import com.simplytest.core.utils.Expected;

public class ContractsDBMock
{
    static private AtomicLong idCounter = new AtomicLong();
    static private ArrayList<Contract> contracts = new ArrayList<>();

    static public ArrayList<Contract> getContracts()
    {
        return contracts;
    }

    static public void clear()
    {
        contracts.clear();
    }

    static public Expected<Contract, Error> createContract(Customer customer)
    {
        var id = new Id(idCounter.incrementAndGet());
        var contract = Contract.create(id, customer, "password");

        if (!contract.successful())
        {
            return Expected.error(contract.error());
        }

        if (isExistingCustomer(customer))
        {
            return Expected.error(Error.AlreadyRegistered);
        }

        contracts.add(contract.value());
        return Expected.success(contract.value());
    }

    static public void removeContract(Id id)
    {
        for (var contract : contracts)
        {
            if (!contract.getId().equals(id))
            {
                continue;
            }

            contracts.remove(contract);
            return;
        }
    }

    static public Contract findContract(Id id)
    {
        for (var contract : contracts)
        {
            if (contract.getId().equals(id))
            {
                return contract;
            }
        }

        return null;
    }

    static public IAccount findAccount(Id id)
    {
        for (var contract : contracts)
        {
            var rtn = contract.getAccount(id);

            if (!rtn.successful())
            {
                continue;
            }

            return rtn.value();
        }

        return null;
    }

    static public boolean isExistingCustomer(Customer customer)
    {
        for (var contract : contracts)
        {
            if (!customer.equals(contract.getCustomer()))
            {
                continue;
            }

            return true;
        }

        return false;
    }
}
