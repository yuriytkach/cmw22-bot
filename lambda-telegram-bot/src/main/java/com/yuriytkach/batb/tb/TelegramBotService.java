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

import static java.util.function.Predicate.not;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;

import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;
import com.yuriytkach.batb.tb.config.AppProperties;
import com.yuriytkach.batb.tb.config.CommandsProperties;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
class TelegramBotService {

  private final AppProperties appProperties;
  private final CommandsProperties commandsProperties;
  private final AccountBalanceStorage accountBalanceStorage;

  @SneakyThrows
  public Optional<Object> processUpdate(final Message message) {
    if (isMyText(message.getText())) {
      final var responseBuilder = SendMessage.builder()
        .chatId(message.getChatId())
        .replyToMessageId(message.getMessageId());

      if (message.getReplyToMessage() != null) {
        responseBuilder.messageThreadId(message.getReplyToMessage().getMessageThreadId());
      }

      if (isHelpCommand(message.getText())) {
        responseBuilder.text("""
          Commands:
          /status - show current account balances
          /help - show this help
          """);
      } else {
        final var accountBalances = accountBalanceStorage.getAll();
        final String text = createAccountBalancesText(accountBalances);
        log.info("Response Text: {}", text);
        responseBuilder.text(text);
      }
      final var response = responseBuilder.build();
      log.info("Sending response: {}", response);
      return Optional.of(response);
    } else if (isMsgForLike(message)) {
      return Optional.of(
        SetMessageReaction.builder()
          .chatId(message.getChatId())
          .messageId(message.getMessageId())
          .isBig(true)
          .reactionTypes(List.of(ReactionTypeEmoji.builder()
            .type(ReactionType.EMOJI_TYPE)
            .emoji("👍")
            .build()
          ))
          .build()
      );
    } else {
      log.info("Message is not for me: {}", message.getMessageId());
      log.debug("Message: {}", message);
      return Optional.empty();
    }
  }

  private boolean isMsgForLike(final Message message) {
    if (message.getForwardFrom() != null && "PrivatBank_help_bot".equals(message.getForwardFrom().getUserName())) {
      log.debug("Message from PrivatBank_help_bot: {}", message.getMessageId());
      return true;
    }
    return false;
  }

  private String createAccountBalancesText(final Set<BankAccountStatus> accountBalances) {
    final long totalUah = StreamEx.of(accountBalances)
      .filter(not(BankAccountStatus::isIgnoreForTotal))
      .map(BankAccountStatus::amountUah)
      .mapToLong(Long::longValue)
      .sum();

    final var builder = new StringBuilder();
    builder.append("🏦 Current account balances:\n\n");
    accountBalances.stream()
      .filter(accountBalance -> accountBalance.amount() > 0 && !accountBalance.isIgnoreForTotal())
      .sorted(Comparator
        .comparing(BankAccountStatus::bankType)
        .thenComparing(BankAccountStatus::amount).reversed()
        .thenComparing(BankAccountStatus::currency)
      )
      .forEach(balance -> {
      final String bankType = switch (balance.bankType()) {
        case PRIVAT -> "🟢";
        case MONO -> "⚫";
        case GSHEET -> "🔵";
      };
      builder.append(bankType).append(" ");
      builder.append(balance.accountName()).append(": ");
      builder.append(balance.currency().getSymbol()).append(" ");
      builder.append("%,.02f".formatted((double)balance.amount() / 100));
      builder.append("\n");
    });
    builder.append("\n");
    builder.append("💰 Total: ₴ ").append("%,.02f".formatted((double)totalUah / 100)).append(" UAH");
    return builder.toString();
  }

  private boolean isMyText(final String text) {
    return isHelpCommand(text) || Stream.of(
      appProperties.botName() + " " + commandsProperties.status(),
      commandsProperties.status() + appProperties.botName()
    ).anyMatch(text::startsWith);
  }

  private boolean isHelpCommand(final String text) {
    return Stream.of(
      appProperties.botName() + " " + commandsProperties.help(),
      commandsProperties.help() + appProperties.botName()
    ).anyMatch(text::startsWith);
  }

}
