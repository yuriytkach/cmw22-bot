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

package com.yuriytkach.batb.dr.tx.mono.token.api;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * <pre>
 *   {
 *     "id": "ZuHWzqkKGVo=",
 *     "time": 1554466347,
 *     "description": "Покупка щастя",
 *     "mcc": 7997,
 *     "hold": false,
 *     "amount": -95000,
 *     "operationAmount": -95000,
 *     "currencyCode": 980,
 *     "commissionRate": 0,
 *     "cashbackAmount": 19000,
 *     "balance": 10050000,
 *     "comment": "За каву",
 *     "counterEdrpou": "3096889974",
 *     "counterIban": "UA898999980000355639201001404",
 *     "counterName": "ТОВАРИСТВО З ОБМЕЖЕНОЮ ВІДПОВІДАЛЬНІСТЮ «ВОРОНА»"
 *   }
 * </pre>
 */
@RegisterForReflection
public record MonoStatement(
  String id,
  long time,
  String description,
  int mcc,
  boolean hold,
  long amount,
  long operationAmount,
  int currencyCode,
  long commissionRate,
  long cashbackAmount,
  long balance,
  String comment,
  String counterEdrpou,
  String counterIban,
  String counterName
) {

}
