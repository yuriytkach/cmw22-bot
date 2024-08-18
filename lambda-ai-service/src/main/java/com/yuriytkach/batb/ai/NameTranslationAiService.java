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
