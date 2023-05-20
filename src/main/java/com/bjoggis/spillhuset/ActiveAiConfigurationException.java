package com.bjoggis.spillhuset;

public class ActiveAiConfigurationException extends RuntimeException {

  public ActiveAiConfigurationException() {
    super("No active AI configuration found");
  }
}
