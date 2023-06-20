package com.bjoggis.spillhuset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpillhusetBotApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpillhusetBotApplication.class, args);
  }
}
