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
      The name can go in the format "Surname Name" or "Name Surname". Output the name in the format "Name Surname".
      If name cannot be translated, then output the original name.
      It is important to have same number of names separated by coma in the output as in the input.
      ---
      {names}
      ---
    """)
  AiResponse translate(final String names);

}
