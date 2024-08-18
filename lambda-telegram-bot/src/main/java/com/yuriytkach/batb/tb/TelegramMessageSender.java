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

import static com.yuriytkach.batb.common.telega.TelegramBotNotificationType.BIRTHDAY;
import static com.yuriytkach.batb.common.telega.TelegramBotNotificationType.DONATION_STATS;
import static com.yuriytkach.batb.common.telega.TelegramBotNotificationType.GENERAL;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.common.telega.TelegramBotNotificationType;
import com.yuriytkach.batb.common.telega.TelegramBotSendMessageRequest;
import com.yuriytkach.batb.tb.api.TelegramApi;
import com.yuriytkach.batb.tb.config.AppProperties;
import com.yuriytkach.batb.tb.config.ChatConfigReader;
import com.yuriytkach.batb.tb.config.ChatConfiguration;
import com.yuriytkach.batb.tb.config.ChatConfiguration.ChatConfig;

import io.vertx.ext.web.handler.sockjs.impl.StringEscapeUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class TelegramMessageSender {

  static final String FORMAT_HTML = "HTML";
  static final String FORMAT_MARKDOWN_V2 = "MarkdownV2";

  private final SecretsReader secretsReader;
  private final AppProperties appProperties;
  private final ChatConfigReader chatConfigReader;

  @RestClient
  private final TelegramApi telegramApi;

  public void sendMessage(final TelegramBotSendMessageRequest request) {
    getChatConfigs().ifPresent(chatConfigs -> secretsReader.readSecret(appProperties.botToken())
      .ifPresent(token -> {
        final var msgAndType = prepareMessage(request);
        log.info("Sending message {} to telegram chats: {}", request.notificationType(), msgAndType);
        StreamEx.of(chatConfigs).forEach(chatConfig -> {
          log.info("Sending message to chat: {}", chatConfig);
          final var msg = request.imgUrl() == null
                          ? createSendMessageRequest(chatConfig, msgAndType)
                          : createSendPhotoRequest(chatConfig, msgAndType, request.imgUrl());

          try (Response response = telegramApi.send(
            token,
            msg instanceof SendPhotoMy spm ? spm.getMethod() : "sendMessage",
            msg
          )) {
            log.info("Response: {}", response.getStatus());
            if (response.hasEntity() && response.getEntity() != null) {
              log.info("Response entity: {}", response.getEntity());
            }
          }
        });
      }));
  }

  private SendMessage createSendMessageRequest(final ChatConfig chatConfig, final MessageData msgAndType) {
    return SendMessage.builder()
      .chatId(chatConfig.id())
      .text(msgAndType.message())
      .parseMode(msgAndType.format())
      .messageThreadId(chatConfig.threads().get(msgAndType.notificationType()))
      .build();
  }

  @SneakyThrows
  private SendPhotoMy createSendPhotoRequest(
    final ChatConfig chatConfig,
    final MessageData msgAndType,
    final String photoUrl
  ) {
    return SendPhotoMy.builder()
      .chatId(chatConfig.id())
      .photo(photoUrl)
      .caption(msgAndType.message())
      .parseMode(msgAndType.format())
      .showCaptionAboveMedia(msgAndType.notificationType != DONATION_STATS)
      .messageThreadId(chatConfig.threads().get(msgAndType.notificationType()))
      .build();
  }

  @SneakyThrows
  private MessageData prepareMessage(final TelegramBotSendMessageRequest request) {
    return switch (request.notificationType()) {
      case BIRTHDAY -> prepareBirthdayMessage(request);
      case DONATION_STATS -> prepareDonationStatsMessage(request);
      default -> new MessageData(request.message(), FORMAT_HTML, GENERAL);
    };
  }

  private MessageData prepareDonationStatsMessage(final TelegramBotSendMessageRequest request) {
    final char[] escape = {'[', ']', '(', ')', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!'};
    final String escaped = request.message().chars()
      .mapToObj(c -> new String(escape).indexOf(c) != -1 ? "\\" + (char) c : String.valueOf((char) c))
      .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
      .toString();
    return new MessageData(escaped, FORMAT_MARKDOWN_V2, DONATION_STATS);
  }

  @SneakyThrows
  private MessageData prepareBirthdayMessage(final TelegramBotSendMessageRequest request) {
    final var msg = StringEscapeUtils.unescapeJava(request.message())
      .replace("Хоку:", "<b>Хоку:</b>")
      .replace("з Днем Народження!", "з <b>Днем Народження!</b> 🥳 ");
    return new MessageData(msg, FORMAT_HTML, BIRTHDAY);
  }

  public Optional<List<ChatConfig>> getChatConfigs() {
    return chatConfigReader.readConfig().map(ChatConfiguration::chats);
  }

  @Builder
  record SendPhotoMy(
    @JsonProperty("chat_id")
    String chatId,
    String photo,
    String caption,
    @JsonProperty("parse_mode")
    String parseMode,
    @JsonProperty("protect_content")
    boolean protectContent,
    @JsonProperty("show_caption_above_media")
    boolean showCaptionAboveMedia,
    @JsonProperty("message_thread_id")
    Integer messageThreadId
  ) {

    @JsonIgnore
    public String getMethod() {
      return "sendPhoto";
    }
  }

  record MessageData(
    String message,
    String format,
    TelegramBotNotificationType notificationType
  ) { }
}
