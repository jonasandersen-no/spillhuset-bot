package com.bjoggis.spillhuset.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class ActiveAiConfiguration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "ai_configuration_id")
  private AiConfiguration aiConfiguration;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AiConfiguration getAiConfiguration() {
    return aiConfiguration;
  }

  public void setAiConfiguration(AiConfiguration aiConfiguration) {
    this.aiConfiguration = aiConfiguration;
  }
}
