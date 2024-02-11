package com.yuriytkach.batb.bsu.bank.gsheet;

import java.util.Map;
import java.util.Set;

import com.yuriytkach.batb.bsu.bank.BankStatusUpdater;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.BankType;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class GSheetBankStatusUpdater implements BankStatusUpdater {

  private static final BankType BANK_TYPE = BankType.GSHEET;

  @Override
  public BankType bankType() {
    return BANK_TYPE;
  }

  @Override
  public void updateAllAccountStatuses(final Map<String, BankAccountStatus> previousStatuses) {

  }

  @Override
  public Set<BankAccountStatus> updateSpecifiedAccountStatuses(final Map<String, BankAccountStatus> specificStatuses) {
    log.info("Returning same account statuses for GSheet: {}", specificStatuses.size());
    return Set.copyOf(specificStatuses.values());
  }
}
