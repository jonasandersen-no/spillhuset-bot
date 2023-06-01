package com.bjoggis.spillhuset.controller;

import com.bjoggis.spillhuset.entity.ActiveAiConfiguration;
import com.bjoggis.spillhuset.entity.AiConfiguration;
import com.bjoggis.spillhuset.repository.ActiveAiConfigurationRepository;
import com.bjoggis.spillhuset.repository.AiConfigurationRepository;
import java.util.Collection;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ai-configurations")
public class AiConfigurationController {

  private final AiConfigurationRepository aiConfigurationRepository;
  private final ActiveAiConfigurationRepository activeAiConfigurationRepository;

  public AiConfigurationController(AiConfigurationRepository aiConfigurationRepository,
      ActiveAiConfigurationRepository activeAiConfigurationRepository) {
    this.aiConfigurationRepository = aiConfigurationRepository;
    this.activeAiConfigurationRepository = activeAiConfigurationRepository;
  }

  @GetMapping("/all")
  public Collection<AiConfiguration> getAll() {
    return aiConfigurationRepository.findAll();
  }

  @GetMapping("/active")
  public AiConfiguration getActive() {
    Optional<ActiveAiConfiguration> active = activeAiConfigurationRepository.findActive();

    return active.map(ActiveAiConfiguration::getAiConfiguration).orElse(null);
  }
}
