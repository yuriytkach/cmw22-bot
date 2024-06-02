package com.yuriytkach.batb.dr.tx.mono.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yuriytkach.batb.dr.DonatorsReporterProperties;

@ExtendWith(MockitoExtension.class)
class MonoTxNameMapperTest {

  @Mock
  private DonatorsReporterProperties donatorsReporterProperties;

  @InjectMocks
  private MonoTxNameMapper tested;

  @ParameterizedTest
  @CsvSource({
    "Від: Степан Бандера, Бандера Степан",
    "Від: STEPAN BANDERA, Bandera Stepan",
    "Від: 🐈, ",
    "Від: Степан, ",
    "Від: P24 CR MD UA, ",
    "Від: P Ihor, ",
    "Від: PP Ihor, ",
    "Від: PP Ihor, ",
    "Від: Hello Ihor, ",
    "Від: Ihor World, "
  })
  void shouldMapDescription(final String description, final String expectedName) {
    lenient().when(donatorsReporterProperties.nameStopWords()).thenReturn(Set.of("Hello", "World"));

    assertThat(tested.mapDonatorName(description)).isEqualTo(Optional.ofNullable(expectedName));
  }
}
