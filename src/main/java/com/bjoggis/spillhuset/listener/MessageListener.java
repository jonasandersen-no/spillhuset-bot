package com.bjoggis.spillhuset.listener;

import com.bjoggis.spillhuset.ChatService;
import com.bjoggis.spillhuset.entity.ThreadChannel;
import com.bjoggis.spillhuset.function.DeleteThreadFunction;
import com.bjoggis.spillhuset.function.SaveMessageFunction;
import com.bjoggis.spillhuset.function.SaveMessageFunction.SaveMessageOptions;
import com.bjoggis.spillhuset.repository.ThreadChannelRepository;
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
  private final SaveMessageFunction saveMessageFunction;
  private final DeleteThreadFunction deleteThreadFunction;

  public MessageListener(ChatService chatService,
      ThreadChannelRepository threadChannelRepository,
      SaveMessageFunction saveMessageFunction,
      DeleteThreadFunction deleteThreadFunction) {
    this.chatService = chatService;
    this.threadChannelRepository = threadChannelRepository;
    this.saveMessageFunction = saveMessageFunction;
    this.deleteThreadFunction = deleteThreadFunction;
  }

  @Override
  public void onChannelDelete(ChannelDeleteEvent event) {
    try {
      net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel threadChannel = event.getChannel()
          .asThreadChannel();

      deleteThreadFunction.accept(
          new DeleteThreadFunction.DeleteThreadOptions(threadChannel.getId()));

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

        saveMessageFunction.accept(new SaveMessageOptions(message.getId(), message.getContentRaw(),
            event.getAuthor().isBot(), threadChannel));

        if (!event.getAuthor().isBot()) {
          event.getChannel().sendTyping().queue(unused -> {
            String response = chatService.chat(content, channel.getId());
            channel.sendMessage(response).queue();
          });
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
