package com.bjoggis.spillhuset;

import com.bjoggis.spillhuset.properties.SpillhusetProperties;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ChatService {

  private final Logger logger = LoggerFactory.getLogger(ChatService.class);

  private final SpillhusetProperties properties;

  public ChatService(SpillhusetProperties properties) {
    this.properties = properties;
  }

  public String chat(String message) {
    OpenAiService service = new OpenAiService(properties.openai().token(), Duration.ofMinutes(1));

    // Count the number of tokens in the message
    long messageTokens = StringUtils.countOccurrencesOf(message, " ") + 1;

    if (messageTokens > 100) {
      logger.warn("Message too long, aborting");
      return "Message too long, please try again with less than 100 words.";
    }

    logger.info("Chat message: " + message);
    ChatCompletionRequest request = ChatCompletionRequest.builder()
        .model("gpt-3.5-turbo")
        .messages(List.of(
            new ChatMessage("system", "You are a helpful assistant."),
            new ChatMessage("user", message)))
        .n(1)
        .maxTokens(250)
        .temperature(1.0)
        .build();
//return "test";

    ChatCompletionResult response = service.createChatCompletion(request);

    logger.info("Message tokens: " + response.getUsage().getPromptTokens());

    long completionTokens = response.getUsage().getCompletionTokens();

    logger.info("Completion tokens used: " + completionTokens);
    String content = response.getChoices().get(0).getMessage().getContent();
    logger.info("Response: " + content);
    return content;
  }
}
