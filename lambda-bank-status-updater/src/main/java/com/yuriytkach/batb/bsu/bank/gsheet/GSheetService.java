package com.yuriytkach.batb.bsu.bank.gsheet;

import static com.yuriytkach.batb.common.Currency.UAH;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.Currency;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class GSheetService {

  private final NumberFormat format = NumberFormat.getInstance(new Locale("uk", "UA"));

  public Optional<Sheets> getSheetsService(final String token) {
    try {
      final GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(token.getBytes()))
        .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));

      final Sheets sheets = new Sheets.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        JacksonFactory.getDefaultInstance(),
        credential
      )
        .setApplicationName("CMW22 Bot")
        .build();
      return Optional.of(sheets);
    } catch (final Exception ex) {
      log.error("Failed to create Google Sheets service: {}", ex.getMessage(), ex);
      return Optional.empty();
    }
  }

  public Optional<GSheetData> readAccountStatus(final BankAccount account, final Sheets sheets) {
    if (account.properties() == null
      || account.properties().isEmpty()
      || !account.properties().containsKey("sheetName")
    ) {
      log.error("GSheet Account has no properties or sheetName is not defined: {}", account);
      return Optional.empty();
    }

    try {
      log.info("Reading google sheet {}, raised cell: {}, spent cell: {}",
        account.id(), account.properties().get("raisedCell"), account.properties().get("spentCell"));

      final String sheetName = account.properties().get("sheetName");

      final Optional<String> raisedStr = readValue(sheets, account.id(), sheetName,
        account.properties().get("raisedCell"));
      final Optional<String> raisedStrUah = readValue(sheets, account.id(), sheetName,
        account.properties().get("raisedUahCell"));

      final Optional<String> spentStr = readValue(sheets, account.id(), sheetName,
        account.properties().get("spentCell"));
      final Optional<String> spentStrUah = readValue(sheets, account.id(), sheetName,
        account.properties().get("spentUahCell"));

      final Optional<String> currStr = readValue(
        sheets, account.id(), sheetName, account.properties().get("currencyCell")
      );

      final Currency currency = currStr.flatMap(Currency::fromString).orElse(UAH);
      final Long raised = raisedStr.map(this::parseLong).orElse(0L);
      final Long spent = spentStr.map(this::parseLong).orElse(0L);

      final var result = GSheetData.builder()
        .sheetId(account.id())
        .raised(raised)
        .raisedUah(raisedStrUah.map(this::parseLong).orElse(currency == UAH ? raised : 0))
        .spent(spent)
        .spentUah(spentStrUah.map(this::parseLong).orElse(currency == UAH ? spent : 0))
        .currency(currency)
        .build();
      log.debug("Read account status: {}", result);

      return Optional.of(result);

    } catch (final Exception ex) {
      log.error("Failed to read account status from Google Sheets: {}", ex.getMessage(), ex);
      return Optional.empty();
    }
  }

  @SneakyThrows
  private Long parseLong(final String value) {
    return format.parse(value).longValue() * 100;
  }

  private Optional<String> readValue(
    final Sheets sheets,
    final String sheetId,
    final String sheetName,
    final String cell
  )
    throws IOException {
    if (cell == null || cell.isBlank()) {
      return Optional.empty();
    }
    final String range = "%s!%s".formatted(sheetName, cell);
    log.debug("Reading cell: {}", range);
    final ValueRange response = sheets.spreadsheets().values()
      .get(sheetId, range)
      .execute();

    final List<List<Object>> values = response.getValues();

    if (values == null || values.isEmpty() || values.get(0).isEmpty()) {
      log.debug("No data found in sheet {} for cell {}", sheetName, cell);
      return Optional.empty();
    } else {
      return Optional.ofNullable(values.get(0).get(0)).map(String.class::cast);
    }
  }

  @Builder
  @RegisterForReflection
  public record GSheetData(String sheetId, long raised, long raisedUah, long spent, long spentUah, Currency currency) { }
}
