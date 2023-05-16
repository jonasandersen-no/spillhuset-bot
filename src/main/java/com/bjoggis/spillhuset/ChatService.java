package com.bjoggis.spillhuset;

import com.bjoggis.spillhuset.entity.Message;
import com.bjoggis.spillhuset.properties.SpillhusetProperties;
import com.bjoggis.spillhuset.repository.MessageRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ChatService {

  private final Logger logger = LoggerFactory.getLogger(ChatService.class);

  private final SpillhusetProperties properties;
  private final MessageRepository messageRepository;


  public ChatService(SpillhusetProperties properties,
      MessageRepository messageRepository) {
    this.properties = properties;
    this.messageRepository = messageRepository;
  }

  @Transactional
  public String chat(String message, String threadId) {
    OpenAiService service = new OpenAiService(properties.openai().token(), Duration.ofMinutes(1));

    // Count the number of tokens in the message
    long messageTokens = StringUtils.countOccurrencesOf(message, " ") + 1;

    if (messageTokens > 100) {
      logger.warn("Message too long, aborting");
      return "Message too long, please try again with less than 100 words.";
    }

    Set<Message> messages = messageRepository.findByThreadChannel_ThreadIdOrderByCreatedAsc(
        threadId);

    List<ChatMessage> chatMessages = new ArrayList<>();
    chatMessages.add(new ChatMessage("system", "You are an helpful assistant that tries to answer like a human"));

    List<ChatMessage> oldMessages = new java.util.ArrayList<>(messages.stream().map(message2 -> {
      switch (message2.getSender()) {
        case USER -> {
          return new ChatMessage("user", message2.getMessage());
        }
        case SYSTEM -> {
          return new ChatMessage("system", message2.getMessage());
        }
        case ASSISTANT -> {
          return new ChatMessage("assistant", message2.getMessage());
        }
      }
      return null;
    }).toList());

    chatMessages.addAll(oldMessages);

    chatMessages.forEach(chatMessage -> logger.info(chatMessage.getRole() + ": " + chatMessage
        .getContent()));
    ChatCompletionRequest request = ChatCompletionRequest.builder()
        .model("gpt-3.5-turbo")
        .messages(chatMessages)
        .n(1)
        .maxTokens(350)
        .temperature(1.0)
        .build();

    ChatCompletionResult response = service.createChatCompletion(request);

    logger.info("Message tokens: " + response.getUsage().getPromptTokens());

    long completionTokens = response.getUsage().getCompletionTokens();

    logger.info("Completion tokens used: " + completionTokens);
    String content = response.getChoices().get(0).getMessage().getContent();
    logger.info("Response: " + content);
    return content;
  }
}
