package com.yuriytkach.batb.dr.stats;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.common.telega.TelegramBotNotificationType;
import com.yuriytkach.batb.common.telega.TelegramBotSendMessageRequest;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
class DonationStatsNotificationService {

  private final SecretsReader secretsReader;
  private final TelegramBotConfig telegramBotConfig;

  @RestClient
  private final TelegramBotApi telegramBotApi;

  void notifyForStats(
    final DonationStats amountStats,
    final DonationStats countStats,
    final int totalTxes,
    final boolean readOnly
  ) {
    final String finalMsg = buildStatsMessage(amountStats, countStats, totalTxes);
    log.debug("Stats message: {}", finalMsg);

    if (readOnly) {
      log.info("Read-only mode, no notifications sent");
    } else {
      sendNotification(finalMsg);
    }
  }

  private String buildStatsMessage(
    final DonationStats amountStats,
    final DonationStats countStats,
    final int totalTxes
  ) {
    return """
      📊 *Статистика донатів за попередній місяць*:
      
      💰 *Загальна сума донатів:* %,.2f грн
      #️⃣ *Кількість транзакцій:* %d
      🏆 *Максимальний донат:* %,.2f грн
      🪙 *Мінімальний донат:* %,.2f грн
      
      📈 *Медіана:* %,.2f (тобто 50%% донатів більша за цю суму)
      📉 *p10:* %,.2f (тобто 10%% донатів менше або рівні цій сумі)
      🥇 *p90:* %,.2f (тобто 10%% донатів більше або рівні цій сумі)
      
      ▶️▶️▶️
      
      🕺 *Кількість донатів від однієї особи* (серед тих, де вдалось визначити їмʼя):
      
      👥 *Унікальні донатери:* %d
      🔝 *Максимальна кількість донатів від однієї особи:* %d
      🔽 *Мінімальна:* %d
      
      📈 *Медіана:* %.0f (тобто 50%% донатили стільки або більше разів)
      🥇 *p90:* %.0f (тобто 10%% донатили стільки або більше разів)
      """.formatted(
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

  private void sendNotification(final String message) {
    secretsReader.readSecret(telegramBotConfig.secretName()).ifPresent(secret -> {
      log.info("Sending donation stats notification to Telegram bot");
      telegramBotApi.sendMessage(
        secret,
        new TelegramBotSendMessageRequest(
          TelegramBotNotificationType.DONATION_STATS,
          message,
          null
        )
      );
    });
  }
}
