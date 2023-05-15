package com.bjoggis.spillhuset.listener;

import com.bjoggis.spillhuset.ChatService;
import com.bjoggis.spillhuset.entity.ThreadChannel;
import com.bjoggis.spillhuset.repository.MessageRepository;
import com.bjoggis.spillhuset.repository.ThreadChannelRepository;
import com.bjoggis.spillhuset.type.Sender;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MessageListener extends ListenerAdapter {

  private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
  private final ChatService chatService;

  private final List<String> messages = List.of("Hei", "Hallo", "God dag", "God morgen",
      "God kveld", "Heisann", "Hei der", "Hei på deg", "Heihei", "Heisveis", "Halla", "Hola",
      "Tjena", "Yo", "Hei igjen");
  private final ThreadChannelRepository threadChannelRepository;
  private final MessageRepository messageRepository;

  public MessageListener(ChatService chatService,
      ThreadChannelRepository threadChannelRepository,
      MessageRepository messageRepository) {
    this.chatService = chatService;
    this.threadChannelRepository = threadChannelRepository;
    this.messageRepository = messageRepository;
  }

  @Override
  @Transactional
  public void onChannelDelete(ChannelDeleteEvent event) {
    try {
      net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel threadChannel = event.getChannel()
          .asThreadChannel();

      Optional<ThreadChannel> threadOpt = threadChannelRepository.findByThreadId(
          threadChannel.getId());
      if (threadOpt.isPresent()) {
        ThreadChannel channel = threadOpt.get();
        messageRepository.deleteByThreadChannel(channel);
        threadChannelRepository.delete(channel);
      }

    } catch (Exception e) {
      //NOOP
      logger.debug(e.getMessage());
    }
  }

  @Override
  @Transactional
  public void onMessageReceived(MessageReceivedEvent event) {

    // Get random message from messages array

    Message message = event.getMessage();
    String content = message.getContentRaw();
    MessageChannelUnion channel = event.getChannel();

    try {
      net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel threadChannel1 = channel.asThreadChannel();
      Optional<com.bjoggis.spillhuset.entity.ThreadChannel> threadOpt =
          threadChannelRepository.findByThreadId(threadChannel1.getId());

      if (threadOpt.isPresent()) {
        ThreadChannel threadChannel = threadOpt.get();

        if (content.toLowerCase().startsWith("!close")) {
          channel.sendMessage("Deleting thread in 5 seconds").queue();
          channel.delete().queueAfter(5, TimeUnit.SECONDS, unused -> {
            threadChannel1.getParentMessageChannel()
                .deleteMessageById(threadChannel.getOriginalMessageId()).queue();
          });
          return;
        }

        com.bjoggis.spillhuset.entity.Message entityMessage = new com.bjoggis.spillhuset.entity.Message();
        entityMessage.setMessageId(message.getId());
        entityMessage.setMessage(message.getContentRaw());
        entityMessage.setCreated(LocalDateTime.now());
        entityMessage.setSender(event.getAuthor().isBot() ? Sender.ASSISTANT : Sender.USER);
        entityMessage.setThreadChannel(threadChannel);

        messageRepository.save(entityMessage);

        if (!event.getAuthor().isBot()) {
          event.getChannel().sendTyping().queue();
          String response = chatService.chat(content, channel.getId());
          channel.sendMessage(response)
              .queue();
        }
      }


    } catch (IllegalStateException e) {
      //NOOP
    } catch (Exception e) {
      logger.error("Something went wrong", e);
      if (event.getAuthor().isBot()) {
        return; // Ignore bots
      }
      channel.sendMessage("Something went wrong, please try again!")
          .queue();
    }

    if (event.getAuthor().isBot()) {
      return; // Ignore bots
    }

    if (content.toLowerCase().startsWith("hei")) {
      String randomMessage = messages.get((int) (Math.random() * messages.size()));
      logger.info("Responding with: " + randomMessage);
      channel.sendMessage(randomMessage)
          .queue();
//    } else if (content.startsWith("!chat ")) {
//      String chatMessage = content.substring(6);
//      String response = chatService.chat(chatMessage, channel.getId());
//      channel.sendMessage(response)
//          .queue();
//    }
    }
  }
}
