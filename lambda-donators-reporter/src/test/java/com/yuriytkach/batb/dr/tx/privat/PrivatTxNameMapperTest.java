package com.yuriytkach.batb.dr.tx.privat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.yuriytkach.batb.dr.tx.privat.api.Transaction;

class PrivatTxNameMapperTest {

  private Transaction createTx(final String description, final String contrAgentName) {
    return new Transaction(
      "id", "curr", "status", "type", "dateTime", "accountId", "sum", "sumEquivalent", description, contrAgentName);
  }

  @Nested
  class DescriptionNameResolverTest {

    private final PrivatTxNameMapper.DescriptionNameResolver tested = new PrivatTxNameMapper.DescriptionNameResolver();

    @ParameterizedTest
    @CsvSource({
      "'Благодійні внески від Бандера Степан Андрійович, Бандера Степан Андрійович', 'Бандера Степан'",
      "'Благодiйнi внески вiд Valentina Brown, Бандера Степан Андрійович', 'Бандера Степан'",
      "'Благодійні внески, Бандера Степан Андрійович', Бандера Степан",
      "'Благодійні внески, БАНДЕРА степАН АНДРІЙОВИЧ', Бандера Степан",
      "'Благодійні внески від проєкту, та ще ось, Бандера Степан Андрійович', Бандера Степан",
      "'Благодійний внесок, Бандера Степан Андрійович', Бандера Степан",
      "'Сплата  за, Бандера Степан Андрійович', Бандера Степан",
      "'на благодійність, Бандера Степан Андрійович', Бандера Степан",
      "4246 **** **** 2003 10.02.2024 21:57:50 Перекази: вiд BANDERA STEPAN, Bandera Stepan",
      "'4246 **** **** 2003 07.02.2024 17:50:31 Послуги: BANDERA, STEPAN, Visa Direct', Bandera Stepan",
      "'4246 **** **** 2003 09.03.2024 03:14:01 Зарахування переказу: BANDERA STEPAN, Visa Direct', Bandera Stepan",

      "4246 **** **** 2003 10.02.2024 21:57:50, ",
      "'4246 **** **** 2003 13.03.2024 20:10:38 Зарахування переказу: OTPBANKP2P, Visa Direct', ",
      "'4246 **** **** 2003 15.02.2024 19:49:11 Перекази: FUIB MoneyTransfer, Visa Direct', ",
      "Благодійні внески від ААА, ",
      "'Благодійний внесок, Бандера', ",
      "'на благодійність, Бандера Степан', ",
      "asdf, ",
    })
    void shouldMapDescription(final String description, final String expectedName) {
      assertThat(tested.resolveName(createTx(description, null))).isEqualTo(Optional.ofNullable(expectedName));
    }
  }

  @Nested
  class ContrAgentNameResolverTest {

    private final PrivatTxNameMapper.ContrAgentNameResolver tested = new PrivatTxNameMapper.ContrAgentNameResolver();

    @ParameterizedTest
    @CsvSource({
      "Бандера Степан Андрійович, Бандера Степан",
      "бандера СТЕПАН Андрійович, Бандера Степан",
      "ТОВ ОУН, ТОВ ОУН",
      "ОУН ТОВ, ОУН ТОВ",

      "ТОВ, ",
      "P2P Yes No, ",
      "123 23 123, ",
      "Бандера Степан, ",
      "Бандера, ",
      "CH_Транз.рахунок платежi bp 4495307, ",
      "БФ СПІВДРУЖНІСТЬ-22 БО, ",
      "Текущий депозит БФ СПІВДРУЖНІСТЬ-22 БО, ",
      "БФ СПІВДРУЖНІСТЬ-22 ТОВ, "
    })
    void shouldMapContrAgent(final String value, final String expectedName) {
      assertThat(tested.resolveName(createTx(null, value))).isEqualTo(Optional.ofNullable(expectedName));
    }
  }

}
