package com.bjoggis.spillhuset.command;

import com.bjoggis.spillhuset.Option;
import jakarta.validation.constraints.NotNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * Base class for all commands. Commands should be named [CommandName]Command. The Command suffix is
 * removed when registering the command.
 */
public abstract class BaseCommand {

  protected final CommandDataImpl commandData;

  private String commandName;

  public BaseCommand(String description) {
    commandName = this.getClass().getSimpleName().toLowerCase();
    commandName = commandName.replace("command", "");
    commandData = new CommandDataImpl(commandName, description);
  }

  public void addOption(Option option) {
    commandData.addOption(option.type(), option.name(), option.description(), option.required(),
        option.autocomplete());
  }

  public CommandDataImpl getCommandData() {
    return commandData;
  }

  public String getCommandName() {
    return commandName;
  }

  public abstract void onSlashCommand(@NotNull SlashCommandInteractionEvent event);
}