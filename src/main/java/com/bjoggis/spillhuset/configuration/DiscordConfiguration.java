package com.bjoggis.spillhuset.configuration;

import com.bjoggis.spillhuset.properties.SpillhusetProperties;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfiguration {

  @Bean
  JDA jda(SpillhusetProperties properties, List<EventListener> listeners)
      throws InterruptedException {
    JDA jda = JDABuilder.createDefault(properties.discord().token(), GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
        .setActivity(Activity.playing(properties.discord().activity().getComment()))
        .addEventListeners(listeners.toArray())
        .build();
    jda.awaitReady();
    return jda;
  }
}
