package com.yuriytkach.batb.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface NameTranslationAiService {

  @SystemMessage("""
    You're an expert in English to Ukrainian language translation.
    """)
  @UserMessage("""
      Your task is to translate into Ukrainian language the provided coma-separated list of names in English.
      Output only the Ukrainian names in the same order as original English names, in a single line with
      all names coma-separated.
      ---
      {names}
      ---
    """)
  AiResponse translate(final String names);

}
