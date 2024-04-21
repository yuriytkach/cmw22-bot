package com.yuriytkach.batb.dr.tx.privat.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Transaction(
  @JsonProperty("REF") String id,
  @JsonProperty("CCY") String curr,
  @JsonProperty("PR_PR") String status,
  @JsonProperty("TRANTYPE") String type,
  @JsonProperty("DATE_TIME_DAT_OD_TIM_P") String dateTime,
  @JsonProperty("AUT_MY_ACC") String accountId,
  @JsonProperty("SUM") String sum,
  @JsonProperty("SUM_E") String sumEquivalent,
  @JsonProperty("OSND") String description,
  @JsonProperty("AUT_CNTR_NAM") String contrAgentName
) {

}
