package com.bjoggis.spillhuset.function;

import com.bjoggis.spillhuset.exception.ActiveAiConfigurationException;
import com.bjoggis.spillhuset.entity.AiConfiguration;
import com.bjoggis.spillhuset.repository.ActiveAiConfigurationRepository;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class ActiveAiConfigurationFunction implements Supplier<AiConfiguration> {

  private final ActiveAiConfigurationRepository repository;

  public ActiveAiConfigurationFunction(
      ActiveAiConfigurationRepository repository) {
    this.repository = repository;
  }

  /**
   * Get the active AI configuration
   * @return the active AI configuration
   * @throws ActiveAiConfigurationException if no active AI configuration is found
   */
  @Override
  public AiConfiguration get() {
    //@formatter:off
    return repository
        .findActive()
          .orElseThrow(ActiveAiConfigurationException::new)
        .getAiConfiguration();
    //@formatter:on
  }
}
