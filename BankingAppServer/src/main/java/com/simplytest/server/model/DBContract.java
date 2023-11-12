package com.simplytest.server.model;

import com.google.gson.Gson;
import com.simplytest.core.Contract;
import com.simplytest.server.json.Json;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "contracts")
public class DBContract
{
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Lob
    @Column(name = "contract")
    private String contract;

    final transient Gson gson = new Gson();

    public DBContract()
    {
    }

    public DBContract(Contract contract)
    {
        this.id = contract.getId().parent();
        this.contract = Json.get().toJson(contract);
    }

    public long id()
    {
        return id;
    }

    public Contract value()
    {
        return Json.get().fromJson(contract, Contract.class);
    }

    public void setContract(Contract contract)
    {
        this.contract = Json.get().toJson(contract);
    }
}
