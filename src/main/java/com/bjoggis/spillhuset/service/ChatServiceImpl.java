package com.bjoggis.spillhuset.service;

import com.bjoggis.spillhuset.entity.AiConfiguration;
import com.bjoggis.spillhuset.entity.Message;
import com.bjoggis.spillhuset.entity.ThreadChannel;
import com.bjoggis.spillhuset.function.ActiveAiConfigurationFunction;
import com.bjoggis.spillhuset.function.SaveMessageFunction;
import com.bjoggis.spillhuset.function.SaveMessageFunction.SaveMessageOptions;
import com.bjoggis.spillhuset.properties.SpillhusetProperties;
import com.bjoggis.spillhuset.repository.AiConfigurationRepository;
import com.bjoggis.spillhuset.repository.MessageRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ChatServiceImpl implements ChatService {

  private final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

  private final SpillhusetProperties properties;
  private final MessageRepository messageRepository;

  private final ActiveAiConfigurationFunction configurationFunction;
  private final AiConfigurationRepository aiConfigurationRepository;
  private final SaveMessageFunction saveMessageFunction;

  public ChatServiceImpl(SpillhusetProperties properties,
      MessageRepository messageRepository,
      ActiveAiConfigurationFunction configurationFunction,
      AiConfigurationRepository aiConfigurationRepository,
      SaveMessageFunction saveMessageFunction) {
    this.properties = properties;
    this.messageRepository = messageRepository;
    this.configurationFunction = configurationFunction;
    this.aiConfigurationRepository = aiConfigurationRepository;
    this.saveMessageFunction = saveMessageFunction;
  }

  @Override
  @Transactional
  public String chat(String messageId, String message, ThreadChannel threadChannel, String userId) {
    AiConfiguration configuration = configurationFunction.get();

    OpenAiService service = new OpenAiService(properties.openai().token(), Duration.ofMinutes(1));

    // Count the number of tokens in the message
    long messageTokens = StringUtils.countOccurrencesOf(message, " ") + 1;

    if (messageTokens > configuration.getRequestMaxTokens()) {
      logger.warn("Message too long, aborting");
      return "Message too long, please try again with less than 100 words.";
    }

    saveMessageFunction.accept(new SaveMessageOptions(messageId, message, false, threadChannel));

    Set<Message> messages = messageRepository.findByThreadChannel_ThreadIdOrderByCreatedAsc(
        threadChannel.getThreadId());

    if (messages.size() > configuration.getMaxMessages()) {
      logger.warn("Too many messages, aborting");
      return "Too many messages, please start a new thread.";
    }

    logger.info("Using system message: " + configuration.getSystemMessage());

    List<ChatMessage> chatMessages = new ArrayList<>();
    chatMessages.add(new ChatMessage("system", configuration.getSystemMessage()));

    List<ChatMessage> oldMessages = new java.util.ArrayList<>(messages.stream().map(message2 -> {
      switch (message2.getSender()) {
        case USER -> {
          return new ChatMessage("user", message2.getMessageAsString());
        }
        case SYSTEM -> {
          return new ChatMessage("system", message2.getMessageAsString());
        }
        case ASSISTANT -> {
          return new ChatMessage("assistant", message2.getMessageAsString());
        }
      }
      return null;
    }).toList());

    chatMessages.addAll(oldMessages);

    chatMessages.forEach(chatMessage -> logger.info(chatMessage.getRole() + ": " + chatMessage
        .getContent()));

    ChatCompletionRequest request = ChatCompletionRequest.builder()
        .model(configuration.getModel())
        .messages(chatMessages)
        .n(configuration.getNumberOfMessages())
        .maxTokens(configuration.getResponseMaxTokens())
        .temperature(configuration.getTemperature())
        .user(hashUserId(userId))
        .build();

    ChatCompletionResult response = service.createChatCompletion(request);

    logger.info("Message tokens: " + response.getUsage().getPromptTokens());

    long completionTokens = response.getUsage().getCompletionTokens();

    logger.info("Completion tokens used: " + completionTokens);
    String content = response.getChoices().get(0).getMessage().getContent();

    saveMessageFunction.accept(new SaveMessageOptions(UUID.randomUUID().toString(), content, true, threadChannel));
    logger.info("Response: " + content);
    return content;
  }

  @Override
  @Transactional(readOnly = true)
  public String generateSingleMessage(String message, String userId, Long configurationId) {
    AiConfiguration configuration = aiConfigurationRepository.findById(configurationId)
        .orElse(configurationFunction.get());

    OpenAiService service = new OpenAiService(properties.openai().token(), Duration.ofMinutes(1));
    logger.info("Using system message: " + configuration.getSystemMessage());

    List<ChatMessage> chatMessages = new ArrayList<>();
    chatMessages.add(new ChatMessage("system", configuration.getSystemMessage()));
    chatMessages.add(new ChatMessage("user", message));

    ChatCompletionRequest request = ChatCompletionRequest.builder()
        .model(configuration.getModel())
        .messages(chatMessages)
        .n(configuration.getNumberOfMessages())
        .maxTokens(configuration.getResponseMaxTokens())
        .temperature(configuration.getTemperature())
        .user(hashUserId(userId))
        .build();

    ChatCompletionResult response = service.createChatCompletion(request);

    logger.info("Message tokens: " + response.getUsage().getPromptTokens());

    long completionTokens = response.getUsage().getCompletionTokens();

    logger.info("Completion tokens used: " + completionTokens);
    String content = response.getChoices().get(0).getMessage().getContent();
    logger.info("Response: " + content);
    return content;
  }

  private String hashUserId(String userId) {
    String hashedUserID = "unknown";
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashedId = digest.digest((userId).getBytes(StandardCharsets.UTF_8));
      hashedUserID = Base64.getEncoder().encodeToString(hashedId);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return hashedUserID;
  }
}
