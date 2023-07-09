package com.bjoggis.spillhuset.service;

import org.springframework.transaction.annotation.Transactional;

public interface ChatService {

  @Transactional
  String chat(String message, String threadId, String userId);
}
