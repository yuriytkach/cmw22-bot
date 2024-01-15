package com.yuriytkach.batb.common.storage;

import java.util.List;

import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.BankType;

public interface BankAccessStorage {

  List<String> getListOfTokens(BankType bankType);

  List<BankAccount> getListOfAccounts(BankType bankType);

}
