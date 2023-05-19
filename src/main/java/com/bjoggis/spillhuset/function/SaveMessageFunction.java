package com.bjoggis.spillhuset.function;

import com.bjoggis.spillhuset.entity.ThreadChannel;
import com.bjoggis.spillhuset.function.SaveMessageFunction.SaveMessageOptions;
import com.bjoggis.spillhuset.repository.MessageRepository;
import com.bjoggis.spillhuset.type.Sender;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class SaveMessageFunction implements Consumer<SaveMessageOptions> {

  private final MessageRepository messageRepository;

  public SaveMessageFunction(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  @Override
  public void accept(SaveMessageOptions options) {

    com.bjoggis.spillhuset.entity.Message entityMessage = new com.bjoggis.spillhuset.entity.Message();
    entityMessage.setMessageId(options.messageId());
    entityMessage.setMessage(options.message());
    entityMessage.setCreated(LocalDateTime.now());
    entityMessage.setSender(options.isBot() ? Sender.ASSISTANT : Sender.USER);
    entityMessage.setThreadChannel(options.threadChannel());

    messageRepository.save(entityMessage);
  }

  public record SaveMessageOptions(String messageId, String message, Boolean isBot,
                                   ThreadChannel threadChannel) {

  }
}
