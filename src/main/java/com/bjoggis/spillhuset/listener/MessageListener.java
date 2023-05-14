package com.bjoggis.spillhuset.listener;

import com.bjoggis.spillhuset.command.BaseCommand;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MessageListener extends ListenerAdapter {

  private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
  private final List<? extends BaseCommand> commands;

  public MessageListener(List<? extends BaseCommand> commands) {
    this.commands = commands;
  }

  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
    Optional<? extends BaseCommand> first =
        commands.stream()
            .filter(baseCommand -> baseCommand.getCommandName().equals(event.getName()))
            .findFirst();
    event
        .deferReply()
        .queue(); // Tell discord we received the command, send a thinking... message to the user

    if (first.isPresent()) {
      first.get().onSlashCommand(event);
      return;

    }

    logger.error("Failed to execute command " + event.getName());
    event.getHook().sendMessage("Failed to execute command").queue();
  }

  @Override
  public void onReady(ReadyEvent event) {
    List<CommandData> commandData =
        commands.stream()
            .map(BaseCommand::getCommandData)
            .collect(Collectors.toList());

    logger.debug("Registering commands: " + commandData.stream().map(CommandData::getName)
        .collect(Collectors.joining(", ")));

    event.getJDA().getGuilds()
        .forEach(guild -> guild.updateCommands()
            .addCommands(commandData)
            .queue());
  }
}