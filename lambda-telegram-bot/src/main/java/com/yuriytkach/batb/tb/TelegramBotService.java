package com.yuriytkach.batb.tb;

import java.util.Optional;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.yuriytkach.batb.tb.config.AppProperties;
import com.yuriytkach.batb.tb.config.CommandsProperties;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
class TelegramBotService {

  private final AppProperties appProperties;
  private final CommandsProperties commandsProperties;

  @SneakyThrows
  public Optional<Object> processUpdate(final Message message) {
    if (isMyText(message.getText())) {
      final SendMessageFromHook response = new SendMessageFromHook();
      response.setChatId(message.getChatId());
      response.setText("""
        I'm alive!
        I will have different stuff here :)
        *Bold text*
        _italic text_
        ||spoiler text||
        🟢 Privat
        ⚫ Mono
        """);
      response.setParseMode("MarkdownV2");
      response.setReplyToMessageId(message.getMessageId());
      if (message.getReplyToMessage() != null) {
        response.setMessageThreadId(message.getReplyToMessage().getMessageThreadId());
      }

      return Optional.of(response);
    } else {
      log.info("Message is not for me. Message id: {}", message.getMessageId());
      return Optional.empty();
    }
  }

  private boolean isMyText(final String text) {
    return text.startsWith(appProperties.botName() + " " + commandsProperties.status());
  }

  @Getter
  public static class SendMessageFromHook extends SendMessage {
    private final String method = "sendMessage";
  }
}
