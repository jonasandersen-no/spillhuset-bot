package com.bjoggis.spillhuset.properties;

import com.bjoggis.spillhuset.type.Activity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "spillhuset")
@Validated
public record SpillhusetProperties(Discord discord, Openai openai, Valorant valorant,
                                   Minecraft minecraft) {


  public record Discord(@NotNull String token, @NotNull Activity activity) {

  }

  public record Openai(@NotNull String token) {

  }

  public record Valorant(@NotNull String webhook) {

  }

  @Valid
  public record Minecraft(@NotNull String username, @NotNull String password,
                          @NotNull @DefaultValue("22") Integer port) {

  }
}
