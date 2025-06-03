package com.simplytest.server.integration;

import com.simplytest.core.Id;
import com.simplytest.core.contracts.Contract;
import com.simplytest.core.customers.CustomerPrivate;
import com.simplytest.server.model.DBContract;
import com.simplytest.server.repo.ContractRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.util.Pair;

import java.util.Calendar;

@TestComponent
public class DbTestHelper {
    @Autowired
    private ContractRepository contractRepository;

    public Contract createContract(){
        var dbEntry = contractRepository.save(new DBContract());
        var birthDay = Calendar.getInstance();
        birthDay.set(2000, 01, 01);

        var contractToSave = Contract.create(new Id(dbEntry.id()) , Instancio.of(CustomerPrivate.class)
                .set(Select.field("birthDay"), birthDay.getTime())
                .create(), "password123").value();

        DBContract dbContract = new DBContract(contractToSave);
        contractRepository.save(dbContract);
        return contractToSave;
    }
    public Pair<Contract, Id> createContractWithRealEstate(){
        var dbEntry = contractRepository.save(new DBContract());
        var birthDay = Calendar.getInstance();
        birthDay.set(2000, 01, 01);

        var contractToSave = Contract.create(new Id(dbEntry.id()) , Instancio.of(CustomerPrivate.class)
                .set(Select.field("birthDay"), birthDay.getTime())
                .create(), "password123").value();
        var id = contractToSave.openRealEstateAccount(1.1, 10.0).first();
        DBContract dbContract = new DBContract(contractToSave);
        contractRepository.save(dbContract);
        return Pair.of(contractToSave, id);
    }

}