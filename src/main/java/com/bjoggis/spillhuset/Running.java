package com.bjoggis.spillhuset;

import com.bjoggis.spillhuset.minecraft.configuration.LinodeRestTemplate;
import com.bjoggis.spillhuset.minecraft.domain.ConnectionInfo;
import com.bjoggis.spillhuset.properties.SpillhusetProperties;
import com.bjoggis.spillhuset.service.SSHService;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.time.Duration;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class Running {

  private boolean isConnected = false;
  private final Logger logger = LoggerFactory.getLogger(Running.class);

  private final LinodeRestTemplate linodeRestTemplate;
  private final SpillhusetProperties properties;

  public Running(LinodeRestTemplate linodeRestTemplate, SpillhusetProperties properties) {
    this.linodeRestTemplate = linodeRestTemplate;
    this.properties = properties;
  }


  @Async
  public void run(ConnectionInfo connectionInfo, InteractionHook hook, String commands,
      Duration duration) {
    if (isConnected) {
      hook.sendMessage("Someone else is already creating a server").queue();
      return;
    }

    while (!isConnected) {
      try {
        logger.info("Waiting for server to be ready. Trying in {} ", duration.toString());
        Thread.sleep(duration.toMillis());
        SSHService sshService = new SSHService(linodeRestTemplate, connectionInfo,
            properties.minecraft());
        logger.info("Connecting to server");
        sshService.setupMinecraft(commands);
        isConnected = true;
        hook.sendMessage("Server should now be ready!").queue();
      } catch (JSchException | IOException | InterruptedException e) {
        logger.error("Failed to connect to server", e);
      }
    }
    isConnected = false;
  }
}
