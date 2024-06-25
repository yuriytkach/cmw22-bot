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
    };
    return processAiResult(result);
  }

  private String processAiResult(final AiResponse result) {
    log.info("AI service response: {}", result.evaluation());
    return switch (result.evaluation()) {
      case POSITIVE -> {
        log.info("AI service successfully translated names");
        yield result.message();
      }
      case NEGATIVE -> {
        log.error("AI service failed to translate names: {}", result.message());
        yield "";
      }
    };
  }
}
