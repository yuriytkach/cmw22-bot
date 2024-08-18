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

package com.yuriytkach.batb.tb;

import org.slf4j.MDC;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;

import com.yuriytkach.batb.tb.config.ChatConfigReader;
import com.yuriytkach.batb.tb.config.ChatConfiguration;

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
public class TelegramHookController {

  private final ChatConfigReader chatConfigReader;
  private final TelegramBotService telegramBotService;

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/event")
  public Response hook(
    @Context com.amazonaws.services.lambda.runtime.Context context,
    final Update body
  ) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Telegram Hook. AWS Request ID: {}", context.getAwsRequestId());

    try {
      if (body.getMessage() != null) {
        if (isValidChatId(body.getMessage().getChat())) {
          log.info(
            "Chat ID is valid: {}. Thread id: {}",
            body.getMessage().getChat().getId(),
            body.getMessage().getMessageThreadId()
          );
          final var response = telegramBotService.processUpdate(body.getMessage());
          return response.map(Response::ok).orElseGet(Response::ok).build();
        } else {
          log.info(
            "Chat ID is invalid: {}. Thread id: {}",
            body.getMessage().getChat().getId(),
            body.getMessage().getMessageThreadId()
          );
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
    return chatConfigReader.readConfig()
      .stream()
      .flatMap(config -> config.chats().stream())
      .map(ChatConfiguration.ChatConfig::id)
      .anyMatch(chatId -> chatId.equals(chat.getId().toString()));
  }

}
