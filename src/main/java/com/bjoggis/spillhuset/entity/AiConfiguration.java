package com.bjoggis.spillhuset.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AiConfiguration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  private String systemMessage;

  private Double temperature;

  private Integer requestMaxTokens;
  private Integer responseMaxTokens;

  private String model;

  private Integer maxMessages;

  private Integer numberOfMessages;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSystemMessage() {
    return systemMessage;
  }

  public void setSystemMessage(String systemMessage) {
    this.systemMessage = systemMessage;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public Integer getRequestMaxTokens() {
    return requestMaxTokens;
  }

  public void setRequestMaxTokens(Integer requestMaxTokens) {
    this.requestMaxTokens = requestMaxTokens;
  }

  public Integer getNumberOfMessages() {
    return numberOfMessages;
  }

  public void setNumberOfMessages(Integer numberOfMessages) {
    this.numberOfMessages = numberOfMessages;
  }

  public Integer getResponseMaxTokens() {
    return responseMaxTokens;
  }

  public void setResponseMaxTokens(Integer responseMaxTokens) {
    this.responseMaxTokens = responseMaxTokens;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public Integer getMaxMessages() {
    return maxMessages;
  }

  public void setMaxMessages(Integer maxMessages) {
    this.maxMessages = maxMessages;
  }

  @Override
  public String toString() {
    return "AiConfiguration{" +
        "id=" + id +
        ", systemMessage='" + systemMessage + '\'' +
        ", temperature=" + temperature +
        ", requestMaxTokens=" + requestMaxTokens +
        ", responseMaxTokens=" + responseMaxTokens +
        ", model='" + model + '\'' +
        ", maxMessages=" + maxMessages +
        ", numberOfMessages=" + numberOfMessages +
        '}';
  }
}
