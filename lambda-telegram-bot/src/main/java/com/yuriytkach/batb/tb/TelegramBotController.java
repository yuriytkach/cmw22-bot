package com.yuriytkach.batb.tb;

import org.slf4j.MDC;

import com.yuriytkach.batb.common.telega.TelegramBotSendMessageRequest;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/bot")
@RequiredArgsConstructor
public class TelegramBotController {

  private final TelegramMessageSender telegramMessageSender;

  @POST
  @Path("/telegram")
  public Response hook(
    @Context com.amazonaws.services.lambda.runtime.Context context,
    final TelegramBotSendMessageRequest request
  ) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Telegram Bot. AWS Request ID: {}", context.getAwsRequestId());

    try {
      log.info("Sending message to Telegram: {}", request.notificationType());
      telegramMessageSender.sendMessage(request);
    } catch (final Exception ex) {
      log.error("Error while sending message: {}", ex.getMessage());
      log.warn("Received request: {}", request);
    }
    return Response.ok().build();
  }
}
