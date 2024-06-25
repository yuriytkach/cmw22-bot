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

  private final SecretsReader secretsReader;
  private final AppProperties appProperties;

  @RestClient
  private final TelegramApi telegramApi;

  public void sendMessage(final TelegramBotSendMessageRequest request) {
    getChatId().ifPresent(chatIds -> {
      secretsReader.readSecret(appProperties.botToken())
        .ifPresent(token -> {
          final String text = prepareMessage(request);
          log.info("Sending message {} to telegram chats: {}", request.notificationType(), text);
          StreamEx.of(chatIds).forEach(chatId -> {
            log.info("Sending message to chat: {}", chatId);
            final var msg = request.imgUrl() == null
                            ? createSendMessageRequest(chatId, text)
                            : createSendPhotoRequest(request, chatId, text);

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
        });
    });
  }

  private SendMessage createSendMessageRequest(final String chatId, final String text) {
    return SendMessage.builder()
      .chatId(chatId)
      .text(text)
      .parseMode("HTML")
      .build();
  }

  @SneakyThrows
  private SendPhotoMy createSendPhotoRequest(
    final TelegramBotSendMessageRequest request,
    final String chatId,
    final String text
  ) {
    return SendPhotoMy.builder()
      .chatId(chatId)
      .photo(request.imgUrl())
      .caption(text)
      .parseMode("HTML")
      .showCaptionAboveMedia(true)
      .build();
  }

  @SneakyThrows
  private String prepareMessage(final TelegramBotSendMessageRequest request) {
    return StringEscapeUtils.unescapeJava(request.message())
      .replace("Хоку:", "<b>Хоку:</b>")
      .replace("з Днем Народження!", "з <b>Днем Народження!</b> 🥳 ");
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
}
