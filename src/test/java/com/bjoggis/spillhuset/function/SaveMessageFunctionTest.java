package com.bjoggis.spillhuset.function;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

import com.bjoggis.spillhuset.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaveMessageFunctionTest {

  @Mock
  MessageRepository repository;

  SaveMessageFunction fn;

  @BeforeEach
  void setUp() {
    fn = new SaveMessageFunction(repository);
  }

  @Test
  void accept() {

    fn.accept(new SaveMessageFunction.SaveMessageOptions("messageId", "message", false, null));

    verify(repository).save(any());
  }
}