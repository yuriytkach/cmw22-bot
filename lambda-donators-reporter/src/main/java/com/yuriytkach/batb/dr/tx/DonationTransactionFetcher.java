package com.yuriytkach.batb.dr.tx;

import java.time.LocalDate;
import java.util.Set;

public interface DonationTransactionFetcher {

  Set<DonationTransaction> fetchTransactions(LocalDate startDate, final LocalDate endDate);

}
