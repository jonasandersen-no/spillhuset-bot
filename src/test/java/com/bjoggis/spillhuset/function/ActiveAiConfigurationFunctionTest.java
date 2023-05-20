package com.bjoggis.spillhuset.function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bjoggis.spillhuset.ActiveAiConfigurationException;
import com.bjoggis.spillhuset.entity.ActiveAiConfiguration;
import com.bjoggis.spillhuset.entity.AiConfiguration;
import com.bjoggis.spillhuset.repository.ActiveAiConfigurationRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActiveAiConfigurationFunctionTest {

  @Mock
  private ActiveAiConfigurationRepository repository;

  private ActiveAiConfigurationFunction fn;

  @BeforeEach
  void setUp() {
    fn = new ActiveAiConfigurationFunction(repository);
  }

  @Test
  void get() {
    ActiveAiConfiguration active = new ActiveAiConfiguration();
    AiConfiguration configuration = new AiConfiguration();
    configuration.setId(1L);
    configuration.setModel("model");
    configuration.setTemperature(0.5);
    configuration.setRequestMaxTokens(1);
    configuration.setResponseMaxTokens(1);
    configuration.setSystemMessage("systemMessage");
    configuration.setMaxMessages(1);
    configuration.setNumberOfMessages(1);

    active.setAiConfiguration(configuration);
    when(repository.findActive()).thenReturn(Optional.of(active));

    AiConfiguration response = fn.get();

    assertThat(response, notNullValue());
    assertThat(response.getId(), equalTo(1L));
    assertThat(response.getModel(), equalTo("model"));
    assertThat(response.getTemperature(), equalTo(0.5));
    assertThat(response.getRequestMaxTokens(), equalTo(1));
    assertThat(response.getResponseMaxTokens(), equalTo(1));
    assertThat(response.getSystemMessage(), equalTo("systemMessage"));
    assertThat(response.getMaxMessages(), equalTo(1));
    assertThat(response.getNumberOfMessages(), equalTo(1));

    verify(repository).findActive();
  }

  @Test
  void getThrowsException() {
    when(repository.findActive()).thenReturn(Optional.empty());

    assertThrows(ActiveAiConfigurationException.class, fn::get);

    verify(repository).findActive();
  }
}