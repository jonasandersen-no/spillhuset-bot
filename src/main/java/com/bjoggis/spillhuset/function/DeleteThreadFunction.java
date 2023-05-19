package com.bjoggis.spillhuset.function;

import com.bjoggis.spillhuset.entity.ThreadChannel;
import com.bjoggis.spillhuset.function.DeleteThreadFunction.DeleteThreadOptions;
import com.bjoggis.spillhuset.repository.MessageRepository;
import com.bjoggis.spillhuset.repository.ThreadChannelRepository;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeleteThreadFunction implements Consumer<DeleteThreadOptions> {

  private final ThreadChannelRepository threadChannelRepository;
  private final MessageRepository messageRepository;

  public DeleteThreadFunction(ThreadChannelRepository threadChannelRepository,
      MessageRepository messageRepository) {
    this.threadChannelRepository = threadChannelRepository;
    this.messageRepository = messageRepository;
  }

  @Override
  @Transactional
  public void accept(DeleteThreadOptions options) {
    Optional<ThreadChannel> threadOpt = threadChannelRepository.findByThreadId(options.threadId());

    threadOpt.ifPresent(messageRepository::deleteByThreadChannel);
    threadOpt.ifPresent(threadChannelRepository::delete);
  }

  public record DeleteThreadOptions(String threadId) {

  }
}
