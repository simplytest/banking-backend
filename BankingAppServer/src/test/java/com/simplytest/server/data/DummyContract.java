package com.simplytest.server.data;

import com.simplytest.core.contracts.Contract;
import com.simplytest.server.json.Json;

public class DummyContract {
    // create dummy contracts with different account settings
    public Contract createDummyContract() {
        String dummy = """
                {
                  "id": {
                    "counter": 1,
                    "parent": 1,
                    "child": 0
                  },
                  "customer": {
                    "type": "com.simplytest.core.customers.CustomerPrivate",
                    "data": {
                      "birthDay": "1985-04-18",
                      "schufaScore": 0.0,
                      "transactionFee": 0.0,
                      "monthlyFee": 2.99,
                      "firstName": "Foo",
                      "lastName": "Bar",
                      "address": {
                        "country": "Deutschland",
                        "zipCode": "12345",
                        "street": "Some Street"
                      }
                    }
                  },
                  "passwordHash": "$2a$12$YkHRZthCXrh/OoXy7QG1suKccQ6pkIh8UlStfWPpbouqOVD0UcEyK",
                  "accounts": [
                    [
                      {
                        "counter": 1,
                        "parent": 1,
                        "child": 1
                      },
                      {
                        "type": "com.simplytest.core.accounts.AccountGiro",
                        "data": {
                          "sendLimit": 3000.0,
                          "dispoLimit": 0.0,
                          "dispoRate": 0.0,
                          "balance": 1000.0,
                          "boundPeriod": 0.0,
                          "interestRate": 0.0
                        }
                      }
                    ]
                  ]
                }
                """;
        return Json.get().fromJson(dummy, Contract.class);
    }

    public Contract createDummyWithRE() {
        var dummy = """
                {
                  "id": {
                    "counter": 2,
                    "parent": 1,
                    "child": 0
                  },
                  "customer": {
                    "type": "com.simplytest.core.customers.CustomerPrivate",
                    "data": {
                      "birthDay": "1985-04-18",
                      "schufaScore": 0.0,
                      "transactionFee": 0.0,
                      "monthlyFee": 2.99,
                      "firstName": "Foo",
                      "lastName": "Bar",
                      "address": {
                        "country": "Deutschland",
                        "zipCode": "12345",
                        "street": "Some Street"
                      }
                    }
                  },
                  "accounts": {
                    "00001:00001": {
                      "type": "com.simplytest.core.accounts.AccountGiro",
                      "data": {
                        "sendLimit": 3000.0,
                        "dispoLimit": 0.0,
                        "dispoRate": 0.0,
                        "balance": 1000.0,
                        "boundPeriod": 0.0,
                        "interestRate": 0.0
                      }
                    },
                    "00001:00002": {
                      "type": "com.simplytest.core.accounts.AccountRealEstate",
                      "data": {
                        "creditAmount": 35000.0,
                        "remainingAmount": 0.0,
                        "payedAmount": 0.0,
                        "runtimeAmount": 0.0,
                        "repaymentRate": 500.0,
                        "monthlyAmount": 1458333.3333333333,
                        "balance": -35000.0,
                        "boundPeriod": 0.0,
                        "interestRate": 0.0
                      }
                    }
                  }
                }""";
        return Json.get().fromJson(dummy, Contract.class);
    }

    public Contract addRealEstateToDummy(Contract dummy, double creditAmount, double repaimentRate) {
        dummy.openRealEstateAccount(repaimentRate, creditAmount);
        return dummy;
    }

    public Contract changeCustomerData(Contract contract, String firstName, String lastName) {
        contract.getCustomer().setFirstName(firstName);
        contract.getCustomer().setLastName(lastName);
        return contract;
    }
}
