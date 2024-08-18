package com.yuriytkach.batb.dr.translation;

import static com.yuriytkach.batb.dr.tx.DonationTransaction.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yuriytkach.batb.common.ai.AiServiceType;
import com.yuriytkach.batb.common.ai.LambdaInvoker;
import com.yuriytkach.batb.dr.AiLambdaConfig;
import com.yuriytkach.batb.dr.Donator;

@ExtendWith(MockitoExtension.class)
class DonatorsNameServiceTest {

  private static final String FUNCTION_NAME = "functionName";

  @Mock
  private LambdaInvoker lambdaInvoker;
  @Mock
  private AiLambdaConfig properties;

  @InjectMocks
  private DonatorsNameService tested;

  @Test
  void shouldTranslateEnglishNamesAndProcessTranslationResponse() {
    final Set<Donator> donators = Set.of(
      new Donator("Bandera Stepan", 100L, 1, Instant.parse("2021-01-01T00:00:00Z")),
      new Donator("Untranslatable Name", 200L, 2, Instant.parse("2021-01-02T00:00:00Z"))
    );

    when(properties.functionName()).thenReturn(FUNCTION_NAME);
    when(lambdaInvoker.invokeAiLambda(any(), any(), any()))
      .thenReturn(Optional.of("\"Бандера Степан,Untranslatable Name\""));

    final var actual = tested.translateEnglishNames(donators);

    assertThat(actual).containsExactlyInAnyOrder(
      new Donator("Степан Бандера", 100L, 1, Instant.parse("2021-01-01T00:00:00Z")),
      new Donator("Name Untranslatable", 200L, 2, Instant.parse("2021-01-02T00:00:00Z"))
    );
    verify(properties).functionName();
    verify(lambdaInvoker).invokeAiLambda(
      FUNCTION_NAME, AiServiceType.TRANSLATE_NAMES, "Bandera Stepan,Untranslatable Name");
  }

  @Test
  void shouldSkipAnonymousAndNonEnglishNamesForTranslation() {
    final Set<Donator> donators = Set.of(
      new Donator("Бандеровець", 100L, 1, Instant.now()),
      new Donator(UNKNOWN, 100L, 1, Instant.now())
    );

    final var actual = tested.translateEnglishNames(donators);

    assertThat(actual).containsExactlyElementsOf(donators);
    verifyNoInteractions(lambdaInvoker, properties);
  }

  @Nested
  class FailedTranslationTests {

    private final Set<Donator> donators = Set.of(
      new Donator("Stepan Bandera", 100L, 1, Instant.parse("2021-01-01T00:00:00Z"))
    );

    @BeforeEach
    void initPropertiesMock() {
      when(properties.functionName()).thenReturn(FUNCTION_NAME);
    }

    @Test
    void shouldHandleFailedLambdaResponse() {
      when(lambdaInvoker.invokeAiLambda(any(), any(), any()))
        .thenReturn(Optional.empty());

      final var actual = tested.translateEnglishNames(donators);

      assertThat(actual).containsExactlyElementsOf(donators);
      verify(lambdaInvoker).invokeAiLambda(FUNCTION_NAME, AiServiceType.TRANSLATE_NAMES, "Stepan Bandera");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "\"\"", "\" \""})
    void shouldHandleEmptyLambdaResponse(final String response) {
      when(lambdaInvoker.invokeAiLambda(any(), any(), any()))
        .thenReturn(Optional.of(response));

      final var actual = tested.translateEnglishNames(donators);

      assertThat(actual).containsExactly(
        new Donator("Bandera Stepan", 100L, 1, Instant.parse("2021-01-01T00:00:00Z"))
      );
      verify(lambdaInvoker).invokeAiLambda(FUNCTION_NAME, AiServiceType.TRANSLATE_NAMES, "Stepan Bandera");
    }

    @Test
    void shouldHandleLambdaResponseWithLessNames() {
      when(lambdaInvoker.invokeAiLambda(any(), any(), any()))
        .thenReturn(Optional.of("\"Бандера Степан\""));

      final var actual = tested.translateEnglishNames(
        Set.of(
          new Donator("Stepan Bandera", 100L, 1, Instant.parse("2021-01-01T00:00:00Z")),
          new Donator("Roman Shukhevych", 200L, 2, Instant.parse("2021-01-02T00:00:00Z"))
        )
      );

      assertThat(actual).containsExactlyInAnyOrder(
        new Donator("Bandera Stepan", 100L, 1, Instant.parse("2021-01-01T00:00:00Z")),
        new Donator("Shukhevych Roman", 200L, 2, Instant.parse("2021-01-02T00:00:00Z"))
      );
      verify(lambdaInvoker).invokeAiLambda(
        FUNCTION_NAME, AiServiceType.TRANSLATE_NAMES, "Roman Shukhevych,Stepan Bandera");
    }

  }

}
