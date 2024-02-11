package com.bjoggis.spillhuset.command.minecraft;

import com.bjoggis.common.discord.command.BaseCommand;
import com.bjoggis.spillhuset.Running;
import com.bjoggis.spillhuset.minecraft.configuration.LinodeRestTemplate;
import com.bjoggis.spillhuset.minecraft.domain.ConnectionInfo;
import com.bjoggis.spillhuset.minecraft.domain.Ip;
import com.bjoggis.spillhuset.minecraft.domain.LinodeInstance;
import java.time.Duration;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdminRetryCommand extends BaseCommand {

  private final Logger logger = LoggerFactory.getLogger(AdminRetryCommand.class);

  private final LinodeRestTemplate linodeRestTemplate;
  private final Running running;

  public AdminRetryCommand(LinodeRestTemplate linodeRestTemplate, Running running) {
    super("Starts a minecraft server");
    this.linodeRestTemplate = linodeRestTemplate;
    this.running = running;
  }

  @Override
  public void onSlashCommand(SlashCommandInteractionEvent event) {
    long idLong = event.getUser().getIdLong();

    long admin = 154689849293668362L;

    if (idLong != admin) {
      event.getHook().sendMessage("You are not allowed to use this command").queue();
      return;
    }

    LinodeInstance info = info(event);

    if (info == null) {
      return;
    }

    ConnectionInfo connectionInfo = new ConnectionInfo(info.id(),
        Ip.from(info.ip()));

    running.run(connectionInfo, event.getHook(), "classpath:start.txt", Duration.ofSeconds(1));
  }

  private LinodeInstance info(SlashCommandInteractionEvent event) {
    LinodeInstance[] instances = linodeRestTemplate.get()
        .getForObject("/instance/list", LinodeInstance[].class);

    if (instances == null || instances.length == 0) {
      event.getHook().sendMessage("""
          The server is not running!
                    
          You can start it by writing `/mstart`
          """).queue();
      return null;
    }

    if (instances.length > 1) {
      logger.warn("More than one server exists, this should not happen");
    }

    if (instances.length == 1) {
      LinodeInstance instance = instances[0];
      event.getHook().sendMessage("""
              **Server info**
              Label: %s
              IP: %s
              Status: %s
              Created: %s
              """.formatted(instance.label(), instance.ip(), instance.status(), instance.created()))
          .queue();
      return instance;
    }
    return null;
  }

}
