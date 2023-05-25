package com.bjoggis.spillhuset.command;

import com.bjoggis.spillhuset.function.DeleteThreadFunction;
import com.bjoggis.spillhuset.function.DeleteThreadFunction.DeleteThreadOptions;
import com.bjoggis.spillhuset.repository.ThreadChannelRepository;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CloseCommand extends BaseCommand {

  private final Logger logger = LoggerFactory.getLogger(CloseCommand.class);
  private final ThreadChannelRepository threadChannelRepository;
  private final DeleteThreadFunction deleteThreadFunction;

  public CloseCommand(
      ThreadChannelRepository threadChannelRepository,
      DeleteThreadFunction deleteThreadFunction) {
    super(
        "Can only be used inside a thread created by the chat command.");
    this.threadChannelRepository = threadChannelRepository;
    this.deleteThreadFunction = deleteThreadFunction;
  }

  @Override
  public void onSlashCommand(@NotNull SlashCommandInteractionEvent event) {
    if (event.getChannel() instanceof ThreadChannel threadChannel) {
      final Optional<com.bjoggis.spillhuset.entity.ThreadChannel> entity =
          threadChannelRepository.findByThreadId(threadChannel.getId());

      if (entity.isEmpty()) {
        event.getHook().sendMessage("This thread is not a chat thread.")
            .setEphemeral(true).queue();
        return;
      }
      logger.info("Deleting thread {}", threadChannel.getId());
      event.getHook().sendMessage("Deleting thread in 5 seconds").queue();
      threadChannel.delete().queueAfter(5, TimeUnit.SECONDS, unused -> {

        final String originalMessageId = entity.get().getOriginalMessageId();
        logger.info("Deleting original message {}", originalMessageId);
        threadChannel.getParentMessageChannel().deleteMessageById(originalMessageId).queue();

        final DeleteThreadOptions option = new DeleteThreadOptions(threadChannel.getId());
        deleteThreadFunction.accept(option);
      });
    } else {
      event.getHook().sendMessage("This thread is not a chat thread.")
          .setEphemeral(true).queue();
    }
  }
}
