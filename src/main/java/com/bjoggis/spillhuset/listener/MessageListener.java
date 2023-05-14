package com.bjoggis.spillhuset.listener;

import com.bjoggis.spillhuset.ChatService;
import java.util.List;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MessageListener extends ListenerAdapter {

  private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
  private final ChatService chatService;

  private final List<String> messages = List.of("Hei", "Hallo", "God dag", "God morgen",
      "God kveld", "Heisann", "Hei der", "Hei på deg", "Heihei", "Heisveis", "Halla", "Hola",
      "Tjena", "Yo", "Hei igjen");

  public MessageListener(ChatService chatService) {
    this.chatService = chatService;
  }


  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return; // Ignore bots
    }

    // Get random message from messages array

    Message message = event.getMessage();
    String content = message.getContentRaw();

    MessageChannelUnion channel = event.getChannel();
    if (content.toLowerCase().startsWith("hei ")) {
      String randomMessage = messages.get((int) (Math.random() * messages.size()));
      logger.info("Responding with: " + randomMessage);
      channel.sendMessage(randomMessage)
          .queue();
    } else if (content.startsWith("!chat ")) {
      String chatMessage = content.substring(6);
      String response = chatService.chat(chatMessage);
      channel.sendMessage(response)
          .queue();
    }
  }
}
