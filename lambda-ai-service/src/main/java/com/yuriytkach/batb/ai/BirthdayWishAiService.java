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
public interface BirthdayWishAiService {

  @SystemMessage("""
    Уяви, що Ти вмієш створювати гарні привітання з нагоди дня народження та користуватись емодзі.
    """)
  @UserMessage("""
      Створи цікаве привітання з Днем Народження для волонтера з нашого фонду,\
      що займається допомогою військовим України. Привітання має бути від щирого серця на 3-5 речень.\
      Не треба багато про подяку волонтеру за роботу, але додай побажань натхнення та перемоги.\
      Також створи жартівливе вітальне хоку з нагоди дня народження волонтера.
      Волонтера звати {name}, для звернення використай ім'я в кличному відмінку.
    """)
  AiResponse createWish(final String name);

}
