package com.yuriytkach.batb.ai;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class AiIT {

  @Inject
  NameTranslationAiService nameTranslationAiService;

  @Test
  void doTranslate() {
    final var rez = nameTranslationAiService.translate("""
      Tkach Yurii
      Chernonordov Andrii
      Huk Igor
      """);
    System.out.println(rez);
  }
}
