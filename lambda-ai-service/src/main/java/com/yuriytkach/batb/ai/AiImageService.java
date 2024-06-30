package com.yuriytkach.batb.ai;

import java.time.Duration;
import java.util.Optional;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import io.quarkiverse.langchain4j.openai.QuarkusOpenAiImageModel;
import io.quarkiverse.langchain4j.openai.runtime.config.LangChain4jOpenAiConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AiImageService {

  private static final String DONATION_STATS_PROMPT = """
    Create a wide landscape image for a social media post.
    The image should represent donation stats for %s, with the word "%s" included prominently.
    The design should be generic with minimal details, incorporating elements that symbolize Ukraine,
    Ukrainian soldiers, and charity donations. Possible elements include:
    - A background featuring the colors of the Ukrainian flag (blue and yellow).
    - Silhouettes or simple outlines of soldiers.
    - A heart or hand symbol to represent charity.
    - Subtle icons like donation boxes or helping hands.
    
    Make sure the design is clean and not overly detailed, focusing on a positive and supportive theme.
    """;

  @Inject
  LangChain4jOpenAiConfig config;

  public AiResponse generateImage(final String month) {
    final LangChain4jOpenAiConfig.OpenAiConfig clientConfig = this.config.defaultConfig();
    final ImageModel model =
      QuarkusOpenAiImageModel.builder()
        .baseUrl(clientConfig.baseUrl())
        .apiKey(clientConfig.apiKey())
        .timeout(Duration.ofMinutes(2))
        .user(clientConfig.imageModel().user())
        .persistDirectory(Optional.empty())
        .modelName("gpt-4o")
        .maxRetries(clientConfig.maxRetries())
        .quality("hd")
        .size("1792x1024")
        .logRequests(true)
        .build();
    final Image image = model.generate(DONATION_STATS_PROMPT.formatted(month, month)).content();

    log.info("Generated image: {}", image.url());
    return new AiResponse(Evaluation.POSITIVE, image.url().toString());
  }

}
