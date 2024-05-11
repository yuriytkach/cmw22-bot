package com.yuriytkach.batb.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface NameTranslationAiService {

  @SystemMessage("""
    You're an expert in English and Ukrainian languages.
    """)
  @UserMessage("""
      Your task is to transliterate Ukrainian names written in English letters and output them in Ukrainian letters.
      Output only the Ukrainian names in the same order as original, in a single line with all names coma-separated.
      ---
      {names}
      ---
    """)
  AiResponse translate(final String names);

}
