package com.yuriytkach.batb.tb;

import java.util.Arrays;

import org.slf4j.MDC;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.tb.config.AppProperties;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/hook")
@RequiredArgsConstructor
public class TelegramBotController {

  private final SecretsReader secretsReader;
  private final AppProperties appProperties;
  private final TelegramBotService telegramBotService;

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/event")
  public Response hook(
    @Context com.amazonaws.services.lambda.runtime.Context context,
    final Update body
  ) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Telegram Book Hook. AWS Request ID: {}", context.getAwsRequestId());

    try {
      if (body.getMessage() != null) {
        if (isValidChatId(body.getMessage().getChat())) {
          log.info("Chat ID is valid");
          final var response = telegramBotService.processUpdate(body.getMessage());
          return response.map(Response::ok).orElseGet(Response::ok).build();
        } else {
          log.info("Chat ID is invalid: {}", body.getMessage().getChat().getId());
          return Response.ok(createDefaultResponse(body)).build();
        }
      } else {
        log.info("Unsupported update: {}", body);
        return Response.ok().build();
      }
    } catch (final Exception ex) {
      log.error("Error while processing update: {}", ex.getMessage());
      log.warn("Received body: {}", body);
    }

    return Response.ok().build();
  }

  private Object createDefaultResponse(final Update body) {
    return SendMessage.builder()
      .chatId(body.getMessage().getChatId())
      .text("Слава Україні! 🇺🇦")
      .build();
  }

  private boolean isValidChatId(final Chat chat) {
    return secretsReader.readSecret(appProperties.chatIdSecretKey())
      .stream()
      .flatMap(chatIds -> Arrays.stream(chatIds.split(",")))
      .anyMatch(chatId -> chatId.equals(chat.getId().toString()));
  }

}
