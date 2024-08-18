/*
 * Copyright (c) 2024  Commonwealth-22
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.yuriytkach.batb.dr.tx.privat.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
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
