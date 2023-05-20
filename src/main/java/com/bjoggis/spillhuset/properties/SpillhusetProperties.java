package com.bjoggis.spillhuset.properties;

import com.bjoggis.spillhuset.type.Activity;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "spillhuset")
@Validated
public record SpillhusetProperties(Discord discord, Openai openai) {


  public record Discord(@NotNull String token, @NotNull Activity activity) {

  }

  public record Openai(@NotNull String token) {

  }
}
