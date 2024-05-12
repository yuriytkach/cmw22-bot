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
