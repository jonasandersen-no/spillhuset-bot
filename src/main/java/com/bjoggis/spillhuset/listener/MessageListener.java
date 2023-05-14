package com.bjoggis.spillhuset.listener;

import java.util.List;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class MessageListener extends ListenerAdapter {

  private final List<String> messages = List.of("Hei", "Hallo", "God dag", "God morgen",
      "God kveld", "Heisann", "Hei der", "Hei på deg", "Heihei", "Heisveis", "Halla", "Hola",
      "Tjena", "Yo", "Hei igjen");


  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return; // Ignore bots
    }

    // Get random message from messages array

    Message message = event.getMessage();
    String content = message.getContentRaw();

    if (content.equalsIgnoreCase("hei")) {
      MessageChannel channel = event.getChannel();
      String randomMessage = messages.get((int) (Math.random() * messages.size()));

      channel.sendMessage(randomMessage)
          .queue();
    }
  }
}
