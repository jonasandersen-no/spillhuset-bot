package com.bjoggis.spillhuset.command;

import com.bjoggis.common.discord.command.BaseCommand;
import com.bjoggis.spillhuset.entity.ThreadChannel;
import com.bjoggis.spillhuset.repository.ThreadChannelRepository;
import java.util.UUID;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChatCommand extends BaseCommand {

  private final Logger logger = LoggerFactory.getLogger(ChatCommand.class);
  private final ThreadChannelRepository threadChannelRepository;

  public ChatCommand(ThreadChannelRepository threadChannelRepository) {
    super("Creates a thread");
    this.threadChannelRepository = threadChannelRepository;
  }

  @Override
  public void onSlashCommand(SlashCommandInteractionEvent event) {
    event.getHook().sendMessage("Creating thread").queue(message -> {
      message.createThreadChannel(
              event.getHook().getInteraction().getMember().getNickname() == null ? event.getHook()
                  .getInteraction().getMember().getEffectiveName()
                  : event.getHook().getInteraction().getMember().getNickname() + UUID.randomUUID().toString().substring(0, 5))
          .queue(threadChannel -> {
            logger.info("Created thread: " + threadChannel.getId());
            ThreadChannel thread = new ThreadChannel();
            thread.setThreadId(threadChannel.getId());
            thread.setOriginalMessageId(message.getId());
            threadChannelRepository.save(thread);
          });

//          .queue(threadChannel -> {
//            threadChannel.sendMessage("Deleting after 5 seconds").queue();
//            threadChannel.delete().queueAfter(5, TimeUnit.SECONDS);
//          });
    });


  }
}
