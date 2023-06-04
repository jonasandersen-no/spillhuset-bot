package com.bjoggis.spillhuset.controller;

import com.bjoggis.spillhuset.entity.ActiveAiConfiguration;
import com.bjoggis.spillhuset.entity.AiConfiguration;
import com.bjoggis.spillhuset.repository.ActiveAiConfigurationRepository;
import com.bjoggis.spillhuset.repository.AiConfigurationRepository;
import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ai-configurations")
public class AiConfigurationController {

  private final Logger logger = LoggerFactory.getLogger(AiConfigurationController.class);

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

  @PutMapping("/active")
  @Transactional
  public ResponseEntity<?> setActive(@RequestParam Long id) {
    logger.info("Received request to set active configuration to {}", id);

    Optional<AiConfiguration> configuration = aiConfigurationRepository.findById(id);

    if (configuration.isEmpty()) {
      logger.warn("Could not find configuration with id {}", id);
      return ResponseEntity.notFound().build();
    }

    final AiConfiguration aiConfiguration = configuration.get();
    final Optional<ActiveAiConfiguration> active = activeAiConfigurationRepository.findActive();

    if (active.isPresent()) {

      if (active.get().getAiConfiguration().getId().equals(aiConfiguration.getId())) {
        logger.info("Configuration is already active, returning 200");
        return ResponseEntity.ok().build();
      }

      logger.info("Found active configuration, updating");
      active.ifPresent(activeAiConfiguration ->
          activeAiConfiguration.setAiConfiguration(aiConfiguration));
      logger.info("Updated active configuration to {}", aiConfiguration);
      activeAiConfigurationRepository.save(active.get());
    }
    logger.info("Request completed successfully,returning 200");
    return ResponseEntity.ok().build();
  }
}
