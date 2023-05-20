package com.bjoggis.spillhuset.repository;

import com.bjoggis.spillhuset.entity.ActiveAiConfiguration;
import com.bjoggis.spillhuset.entity.AiConfiguration;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ActiveAiConfigurationRepository extends
    JpaRepository<ActiveAiConfiguration, AiConfiguration> {

  @Query("select a from ActiveAiConfiguration a")
  Optional<ActiveAiConfiguration> findActive();
}