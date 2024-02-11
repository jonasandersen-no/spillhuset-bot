package com.bjoggis.spillhuset.command.minecraft;

import com.bjoggis.common.discord.command.BaseCommand;
import com.bjoggis.spillhuset.minecraft.configuration.LinodeRestTemplate;
import com.bjoggis.spillhuset.minecraft.domain.ConnectionInfo;
import com.bjoggis.spillhuset.minecraft.domain.Ip;
import com.bjoggis.spillhuset.minecraft.domain.LinodeInstance;
import com.bjoggis.spillhuset.properties.SpillhusetProperties;
import com.bjoggis.spillhuset.service.SSHService;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MstopCommand extends BaseCommand {

  private final Logger logger = LoggerFactory.getLogger(MstopCommand.class);
  private final LinodeRestTemplate linodeRestTemplate;
  private final SpillhusetProperties properties;

  public MstopCommand(LinodeRestTemplate linodeRestTemplate, SpillhusetProperties properties) {
    super("Stops the minecraft server");
    this.linodeRestTemplate = linodeRestTemplate;
    this.properties = properties;
  }

  @Override
  public void onSlashCommand(SlashCommandInteractionEvent event) {
    logger.info("Stopping server");
    RestTemplate restTemplate = linodeRestTemplate.get();

    LinodeInstance[] instances = restTemplate.getForObject("/instance/list",
        LinodeInstance[].class);

    if (instances == null || instances.length == 0) {
      event.getHook().sendMessage("Server does not exist").queue();
      return;
    }

    for (LinodeInstance instance : instances) {
      ConnectionInfo connectionInfo = new ConnectionInfo(instance.id(), Ip.from(instance.ip()));
      try {
        SSHService sshService = new SSHService(linodeRestTemplate, connectionInfo,
            properties.minecraft());

        sshService.runShellCommands("classpath:stop.txt");
      } catch (JSchException | IOException e) {
        event.getHook().sendMessage("Failed to stop server").queue();
        logger.error("Failed to stop server", e);
      }
      restTemplate.delete("/instance/%d".formatted(instance.id()));
    }
    event.getHook().sendMessage("""
        Server is now stopped!.
        
        You can start it again by writing `/mstart`
        """).queue();

  }
}
