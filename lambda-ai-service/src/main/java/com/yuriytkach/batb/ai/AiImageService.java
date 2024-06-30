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
    A wide landscape image designed for a social media post representing donation stats for %s. \
    The background features the colors of the Ukrainian flag, with the top half in blue and the bottom half in yellow. \
    Prominently, the word '%s' is displayed in a clean, bold font. Silhouettes of Ukrainian soldiers are placed \
    on the blue section, facing forward with minimal detail. A heart symbol and hand icons representing charity \
    are included, subtly integrated within the design. Additional subtle icons, like donation boxes and helping hands, \
    are scattered lightly across the image. The overall design is clean and supportive, conveying a positive message.
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
        .modelName("dall-e-3")
        .maxRetries(clientConfig.maxRetries())
        .quality("standard")
        .size("1792x1024")
        .logRequests(true)
        .build();
    final Image image = model.generate(DONATION_STATS_PROMPT.formatted(month, month)).content();

    log.info("Generated image: {}", image.url());
    return new AiResponse(Evaluation.POSITIVE, image.url().toString());
  }

}
