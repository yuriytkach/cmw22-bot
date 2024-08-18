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

package com.yuriytkach.batb.bc.notify;

import static java.util.function.Predicate.not;

import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.yuriytkach.batb.bc.Person;
import com.yuriytkach.batb.common.ai.AiServiceType;
import com.yuriytkach.batb.common.ai.LambdaInvoker;
import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.common.telega.TelegramBotNotificationType;
import com.yuriytkach.batb.common.telega.TelegramBotSendMessageRequest;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class BirthdayNotificationService {

  private final LambdaInvoker lambdaInvoker;
  private final AiLambdaConfig aiLambdaConfig;

  private final SecretsReader secretsReader;
  private final TelegramBotConfig telegramBotConfig;

  @RestClient
  private final TelegramBotApi telegramBotApi;

  public void notifyAboutBirthday(final Person person, final boolean readOnly) {
    log.info("Happy birthday, {}!", person.name());

    final Optional<String> birthdayMessage = retrieveBirthdayMessage(person);
    log.debug("Birthday message: {}", birthdayMessage.orElse("No message"));

    final var finalMsg = birthdayMessage
      .filter(not(msg -> msg.contains("error")))
      .orElseGet(() -> genDefaultMessage(person));

    if (readOnly) {
      log.info("Read-only mode, no notifications sent");
    } else {
      sendNotification(person, finalMsg);
    }
  }

  private String genDefaultMessage(final Person person) {
    return """
      %s! \\uD83C\\uDF89 Вітаємо тебе з Днем Народження! Бажаємо тобі невичерпного натхнення,
      міцного здоров'я та здійснення всіх мрій. Нехай кожен день приносить нові перемоги,
      а в серці завжди палає вогонь добра та віри.
      
      Хоку:
      Святковий день твій,
      Волонтерська сила є - 
      Торт і перемога! \\uD83C\\uDF82
      """.formatted(person.name());
  }

  private void sendNotification(final Person person, final String birthdayMessage) {
    secretsReader.readSecret(telegramBotConfig.secretName()).ifPresent(secret -> {
      log.info("Sending birthday notification to Telegram bot");
      telegramBotApi.sendMessage(
        secret,
        new TelegramBotSendMessageRequest(
          TelegramBotNotificationType.BIRTHDAY,
          birthdayMessage,
          person.imgUrl()
        )
      );
    });
  }

  private Optional<String> retrieveBirthdayMessage(final Person person) {
    final String payload = person.name().split("\\s+")[0];

    return lambdaInvoker.invokeAiLambda(
      aiLambdaConfig.functionName(),
      AiServiceType.BIRTHDAY_WISH,
      payload
    );
  }
}
