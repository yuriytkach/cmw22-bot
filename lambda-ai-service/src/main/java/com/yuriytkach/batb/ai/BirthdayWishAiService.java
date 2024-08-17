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
