package com.bjoggis.spillhuset;

import com.bjoggis.spillhuset.listener.MessageListener;
import com.bjoggis.spillhuset.properties.SpillhusetProperties;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfiguration {

  @Bean
  JDA jda(SpillhusetProperties  properties, MessageListener listener) throws InterruptedException {
    JDA jda = JDABuilder.createDefault(properties.discord().token())
        .setActivity(Activity.playing(properties.discord().activity().getComment()))
        .addEventListeners(listener)
        .build();
    jda.awaitReady();
    return jda;
  }
}
