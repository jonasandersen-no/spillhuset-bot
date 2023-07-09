package com.bjoggis.spillhuset.listener;

import com.bjoggis.spillhuset.exception.ActiveAiConfigurationException;
import com.bjoggis.spillhuset.service.ChatService;
import com.bjoggis.spillhuset.entity.ThreadChannel;
import com.bjoggis.spillhuset.function.SaveMessageFunction;
import com.bjoggis.spillhuset.function.SaveMessageFunction.SaveMessageOptions;
import com.bjoggis.spillhuset.repository.ThreadChannelRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
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

  private final List<String> messages = List.of(
      "Hei",
      "Hallo",
      "God dag",
      "God morgen",
      "God kveld",
      "Hi",
      "Hello",
      "Hey",
      "Greetings",
      "Howdy",
      "Здравствуйте (Zdravstvuyte)",
      "Привет (Privet)",
      "Добрый день (Dobryy den')",
      "Доброе утро (Dobroye utro)",
      "Добрый вечер (Dobryy vecher)",
      "Hola",
      "Buenos días",
      "Buenas tardes",
      "Buenas noches",
      "¿Qué tal?",
      "Hallais",
      "Heisann",
      "Tjena",
      "Sjekk deg",
      "Morra",
      "Aloha",
      "Salutations",
      "Sup",
      "Bonjour",
      "G'day",
      "Здорово (Zdorovo)",
      "Приветик (Privetik)",
      "Здравствуй (Zdravstvuy)",
      "День добрый (Den' dobryy)",
      "Вечер добрый (Vecher dobryy)",
      "¡Hola, qué haces!",
      "¿Qué pasa?",
      "¡Bien!",
      "Cześć!",
      "Witaj!",
      "Dobry wieczór!",
      "Dzień dobry!");
  private final ThreadChannelRepository threadChannelRepository;
  private final SaveMessageFunction saveMessageFunction;

  public MessageListener(ChatService chatService,
      ThreadChannelRepository threadChannelRepository,
      SaveMessageFunction saveMessageFunction) {
    this.chatService = chatService;
    this.threadChannelRepository = threadChannelRepository;
    this.saveMessageFunction = saveMessageFunction;
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

        saveMessageFunction.accept(new SaveMessageOptions(message.getId(), message.getContentRaw(),
            event.getAuthor().isBot(), threadChannel));

        if (!event.getAuthor().isBot()) {
          //Scheduler initialization
          ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
          scheduler.scheduleAtFixedRate(() -> {
            logger.info("Sending typing");
            event.getChannel().sendTyping().queue();
          }, 0, 5, TimeUnit.SECONDS);

          String response = chatService.chat(content, channel.getId(), event.getAuthor().getId());
          scheduler.shutdown();
          channel.sendMessage(response).queue();
        }
      }


    } catch (IllegalStateException e) {
      //NOOP
    } catch (ActiveAiConfigurationException e) {
      logger.warn("Can't find active AI configuration");
      if (event.getAuthor().isBot()) {
        return; // Ignore bots
      }
      channel.sendMessage("Can't find active AI configuration. Please contact an admin!")
          .queue();
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
