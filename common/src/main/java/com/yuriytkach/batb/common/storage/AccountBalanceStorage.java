package com.yuriytkach.batb.common.storage;

import java.util.Set;

import com.yuriytkach.batb.common.BankAccountStatus;

public interface AccountBalanceStorage {

  void saveAll(Set<BankAccountStatus> bankAccountStatuses);

}
