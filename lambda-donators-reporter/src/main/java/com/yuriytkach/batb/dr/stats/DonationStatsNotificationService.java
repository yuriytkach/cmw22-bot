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

package com.yuriytkach.batb.dr.stats;

import java.time.Month;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.yuriytkach.batb.common.ai.AiServiceType;
import com.yuriytkach.batb.common.ai.LambdaInvoker;
import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.common.telega.TelegramBotNotificationType;
import com.yuriytkach.batb.common.telega.TelegramBotSendMessageRequest;
import com.yuriytkach.batb.dr.AiLambdaConfig;
import com.yuriytkach.batb.dr.DonatorsReporterProperties;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
class DonationStatsNotificationService {

  private final SecretsReader secretsReader;
  private final TelegramBotConfig telegramBotConfig;

  private final LambdaInvoker lambdaInvoker;
  private final AiLambdaConfig aiLambdaConfig;

  private final DonatorsReporterProperties properties;

  @RestClient
  private final TelegramBotApi telegramBotApi;

  void notifyForStats(
    final DonationStats amountStats,
    final DonationStats countStats,
    final int totalTxes,
    final Month month,
    final boolean readOnly
  ) {
    final String finalMsg = buildStatsMessage(amountStats, countStats, totalTxes);
    log.debug("Stats message:\n{}", finalMsg);

    if (readOnly) {
      log.info("Read-only mode, no notifications sent");
    } else {
      sendNotification(finalMsg, month);
    }
  }

  private String buildStatsMessage(
    final DonationStats amountStats,
    final DonationStats countStats,
    final int totalTxes
  ) {
    return properties.donationStatsNotificationMessage().formatted(
      amountStats.total() / 100.0,
      totalTxes,
      amountStats.max() / 100.0,
      amountStats.min() / 100.0,
      amountStats.median() / 100.0,
      amountStats.p10() / 100.0,
      amountStats.p90() / 100.0,
      countStats.total(),
      countStats.max(),
      countStats.min(),
      countStats.median(),
      countStats.p90()
    );
  }

  private Optional<String> retrieveImageUrl(final Month month) {
    final String payload = month.name();

    try {
      return lambdaInvoker.invokeAiLambda(
        aiLambdaConfig.functionName(),
        AiServiceType.IMAGE_DONATION_STATS,
        payload
      ).map(imgUrl -> imgUrl.substring(1, imgUrl.length()-1));
    } catch (final Exception ex) {
      log.error("Failed to retrieve image URL for donation stats: {}", ex.getMessage());
      return Optional.empty();
    }
  }

  private void sendNotification(final String message, final Month month) {
    secretsReader.readSecret(telegramBotConfig.secretName()).ifPresent(secret -> {

      final Optional<String> imageUrl = retrieveImageUrl(month);

      log.info("Sending donation stats notification to Telegram bot with image: {}", imageUrl);
      telegramBotApi.sendMessage(
        secret,
        new TelegramBotSendMessageRequest(
          TelegramBotNotificationType.DONATION_STATS,
          message,
          imageUrl.orElse(null)
        )
      );
    });
  }
}
