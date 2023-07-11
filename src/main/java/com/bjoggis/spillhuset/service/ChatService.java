package com.bjoggis.spillhuset.service;

 import com.bjoggis.spillhuset.entity.AiConfiguration;
import org.springframework.transaction.annotation.Transactional;

public interface ChatService {

  @Transactional
  String chat(String message, String threadId, String userId);

  /**
   * Generates a single message from openai. This message is not stored in the database.
   * @param message The message to generate from
   * @param userId The user id to whom the message is generated for
   * @param configurationId The configuration to use
   * @return The generated message
   */
  @Transactional
  String generateSingleMessage(String message, String userId, Long configurationId);
}
