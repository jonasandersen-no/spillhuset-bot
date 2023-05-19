package com.bjoggis.spillhuset.function;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bjoggis.spillhuset.entity.ThreadChannel;
import com.bjoggis.spillhuset.repository.MessageRepository;
import com.bjoggis.spillhuset.repository.ThreadChannelRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteThreadFunctionTest {

  @Mock
  private ThreadChannelRepository threadChannelRepository;

  @Mock
  private MessageRepository messageRepository;

  private DeleteThreadFunction fn;

  @BeforeEach
  void setUp() {
    fn = new DeleteThreadFunction(threadChannelRepository, messageRepository);
  }

  @Test
  void accept() {
    when(threadChannelRepository.findByThreadId("threadId"))
        .thenReturn(Optional.of(new ThreadChannel()));

    fn.accept(new DeleteThreadFunction.DeleteThreadOptions("threadId"));

    verify(threadChannelRepository).findByThreadId("threadId");
    verify(messageRepository).deleteByThreadChannel(any());
    verify(threadChannelRepository).delete(any());

  }
}