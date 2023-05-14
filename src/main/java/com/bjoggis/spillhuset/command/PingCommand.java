package com.bjoggis.spillhuset.command;

import com.bjoggis.spillhuset.model.Option;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PingCommand extends BaseCommand {

  public PingCommand() {
    super("Test command to check if the bot is responding");
    addOption(new Option("message", "Message will be sent back", OptionType.STRING, false, false));
  }

  @Override
  public void onSlashCommand(SlashCommandInteractionEvent event) {
    OptionMapping messageOption = event.getOption("message");
    if (messageOption == null) {
      event.getHook().sendMessage("Pong!").queue();
      return;
    }

    String message = messageOption.getAsString();

    if (StringUtils.hasText(message)) {
      event.getHook().sendMessage(message).queue();
      return;
    }
  }
}
