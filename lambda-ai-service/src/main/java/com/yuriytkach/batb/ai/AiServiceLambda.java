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

  @Override
  public String handleRequest(final LambdaAiServiceRequest request, final Context context) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("AI Service Lambda. AWS Request ID: {}", context.getAwsRequestId());
    log.debug("Request: {}", request);

    if ("translateNames".equals(request.service())) {
      log.info("Invoking AI service to translate names");
      final var result = nameTranslationAiService.translate(request.payload());
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
    } else {
      log.error("Unknown service: {}", request.service());
      return "";
    }
  }
}
