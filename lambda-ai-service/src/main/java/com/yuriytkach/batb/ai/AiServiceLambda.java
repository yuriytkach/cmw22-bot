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

package com.yuriytkach.batb.ai;

import org.slf4j.MDC;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named("aiService")
public class AiServiceLambda implements RequestHandler<LambdaAiServiceRequest, String> {

  @Inject
  NameTranslationAiService nameTranslationAiService;

  @Inject
  BirthdayWishAiService birthdayWishAiService;

  @Inject
  AiImageService aiImageService;

  @Override
  public String handleRequest(final LambdaAiServiceRequest request, final Context context) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("AI Service Lambda. AWS Request ID: {}", context.getAwsRequestId());
    log.debug("Request: {}", request);

    final AiResponse result = switch (request.service()) {
      case TRANSLATE_NAMES -> {
        log.info("Invoking AI service to translate names");
        yield nameTranslationAiService.translate(request.payload());
      }
      case BIRTHDAY_WISH -> {
        log.info("Invoking AI service to create birthday wish");
        yield birthdayWishAiService.createWish(request.payload());
      }
      case IMAGE_DONATION_STATS -> {
        log.info("Invoking AI service for image generation for donation stats for : {}", request.payload());
        yield aiImageService.generateImage(request.payload());
      }
    };
    return processAiResult(result);
  }

  private String processAiResult(final AiResponse result) {
    log.info("AI service response: {}", result.evaluation());
    return switch (result.evaluation()) {
      case POSITIVE -> {
        log.info("AI service success");
        yield result.message();
      }
      case NEGATIVE -> {
        log.error("AI service failed: {}", result.message());
        yield "";
      }
    };
  }
}
