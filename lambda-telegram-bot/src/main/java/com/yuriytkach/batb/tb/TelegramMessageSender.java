package com.yuriytkach.batb.tb;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.common.telega.TelegramBotSendMessageRequest;
import com.yuriytkach.batb.tb.api.TelegramApi;
import com.yuriytkach.batb.tb.config.AppProperties;

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

  @RestClient
  private final TelegramApi telegramApi;

  public void sendMessage(final TelegramBotSendMessageRequest request) {
    getChatId().ifPresent(chatIds -> secretsReader.readSecret(appProperties.botToken())
      .ifPresent(token -> {
        final var msgAndType = prepareMessage(request);
        log.info("Sending message {} to telegram chats: {}", request.notificationType(), msgAndType);
        StreamEx.of(chatIds).forEach(chatId -> {
          log.info("Sending message to chat: {}", chatId);
          final var msg = request.imgUrl() == null
                          ? createSendMessageRequest(chatId, msgAndType)
                          : createSendPhotoRequest(chatId, msgAndType, request.imgUrl());

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

  private SendMessage createSendMessageRequest(final String chatId, final MessageWithFormat msgAndType) {
    return SendMessage.builder()
      .chatId(chatId)
      .text(msgAndType.message())
      .parseMode(msgAndType.format())
      .build();
  }

  @SneakyThrows
  private SendPhotoMy createSendPhotoRequest(
    final String chatId,
    final MessageWithFormat msgAndType,
    final String photoUrl
  ) {
    return SendPhotoMy.builder()
      .chatId(chatId)
      .photo(photoUrl)
      .caption(msgAndType.message())
      .parseMode(msgAndType.format())
      .showCaptionAboveMedia(true)
      .build();
  }

  @SneakyThrows
  private MessageWithFormat prepareMessage(final TelegramBotSendMessageRequest request) {
    return switch (request.notificationType()) {
      case BIRTHDAY -> prepareBirthdayMessage(request);
      case DONATION_STATS -> prepareDonationStatsMessage(request);
      default -> new MessageWithFormat(request.message(), FORMAT_HTML);
    };
  }

  private MessageWithFormat prepareDonationStatsMessage(final TelegramBotSendMessageRequest request) {
    final char[] escape = {'[', ']', '(', ')', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!'};
    final String escaped = request.message().chars()
      .mapToObj(c -> new String(escape).indexOf(c) != -1 ? "\\" + (char) c : String.valueOf((char) c))
      .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
      .toString();
    return new MessageWithFormat(escaped, FORMAT_MARKDOWN_V2);
  }

  @SneakyThrows
  private MessageWithFormat prepareBirthdayMessage(final TelegramBotSendMessageRequest request) {
    final var msg = StringEscapeUtils.unescapeJava(request.message())
      .replace("Хоку:", "<b>Хоку:</b>")
      .replace("з Днем Народження!", "з <b>Днем Народження!</b> 🥳 ");
    return new MessageWithFormat(msg, FORMAT_HTML);
  }

  public Optional<List<String>> getChatId() {
    return secretsReader.readSecret(appProperties.chatIdSecretKey())
      .map(ids -> List.of(ids.split(",")));
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
    boolean showCaptionAboveMedia
  ) {

    @JsonIgnore
    public String getMethod() {
      return "sendPhoto";
    }
  }

  record MessageWithFormat(
    String message,
    String format
  ) { }
}
