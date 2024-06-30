package com.yuriytkach.batb.dr.stats;

record DonationStats(
  long total,
  long max,
  long min,
  double avg,
  double median,
  double p90,
  double p10
) {

}
