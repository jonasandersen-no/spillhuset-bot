package com.bjoggis.spillhuset.command.minecraft;

import com.bjoggis.common.discord.command.BaseCommand;
import com.bjoggis.spillhuset.minecraft.domain.LinodeInstance;
import com.bjoggis.spillhuset.minecraft.configuration.LinodeRestTemplate;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MstatusCommand extends BaseCommand {

  private final Logger logger = LoggerFactory.getLogger(MstatusCommand.class);
  private final LinodeRestTemplate linodeRestTemplate;

  public MstatusCommand(LinodeRestTemplate linodeRestTemplate) {
    super("Checks status on minecraft server");
    this.linodeRestTemplate = linodeRestTemplate;
  }

  @Override
  public void onSlashCommand(SlashCommandInteractionEvent event) {
    info(event);

  }

  private void info(SlashCommandInteractionEvent event) {
    LinodeInstance[] instances = linodeRestTemplate.get()
        .getForObject("/instance/list", LinodeInstance[].class);

    if (instances == null || instances.length == 0) {
      event.getHook().sendMessage("""
          The server is not running!
                    
          You can start it by writing `/mstart`
          """).queue();
      return;
    }

    if (instances.length > 1) {
      logger.warn("More than one server exists, this should not happen");
    }

    if (instances.length == 1) {
      LinodeInstance instance = instances[0];
      event.getHook().sendMessage("""
              **Server info**
              Hostname: **minecraft.jonasandersen.no**
              Label: %s
              IP: %s
              Status: %s
              Created: %s
              """.formatted(instance.label(), instance.ip(), instance.status(), instance.created()))
          .queue();
      return;
    }

    StringBuilder builder = new StringBuilder();
    for (LinodeInstance instance : instances) {
      builder.append(instance.label()).append(" ").append(instance.ip()).append("\n");
    }

    event.getHook().sendMessage("List of instances:\n%s".formatted(builder.toString())).queue();
  }
}
