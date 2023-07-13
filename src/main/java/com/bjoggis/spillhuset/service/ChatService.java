package com.bjoggis.spillhuset.service;

import com.bjoggis.spillhuset.entity.ThreadChannel;
import org.springframework.transaction.annotation.Transactional;

public interface ChatService {

  /**
   * Chat with the bot.
   * @param messageId The message id
   * @param message The message
   * @param threadChannel The thread channel
   * @param userId  The user id
   * @return The response from the bot
   */
  @Transactional
  String chat(String messageId, String message, ThreadChannel threadChannel, String userId);

  /**
   * Generates a single message from openai. This message is not stored in the database.
   *
   * @param message         The message to generate from
   * @param userId          The user id to whom the message is generated for
   * @param configurationId The configuration to use
   * @return The generated message
   */
  @Transactional
  String generateSingleMessage(String message, String userId, Long configurationId);
}
