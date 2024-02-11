package com.bjoggis.spillhuset;

import com.bjoggis.spillhuset.command.minecraft.Mstartcommand.CreateLResponse;
import com.bjoggis.spillhuset.minecraft.configuration.LinodeRestTemplate;
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

  private final Duration duration = Duration.ofMinutes(3);

  public Running(LinodeRestTemplate linodeRestTemplate, SpillhusetProperties properties) {
    this.linodeRestTemplate = linodeRestTemplate;
    this.properties = properties;
  }


  @Async
  public void run(CreateLResponse createResponse, InteractionHook hook) {

    if (isConnected) {
      hook.sendMessage("Someone else is already creating a server").queue();
      return;
    }

    while (!isConnected) {
      try {
        logger.info("Waiting for server to be ready. Trying in {} ", duration.toString());
        Thread.sleep(duration.toMillis());
        SSHService sshService = new SSHService(linodeRestTemplate, createResponse, hook, properties.minecraft());
        logger.info("Connecting to server");
        sshService.setupMinecraft();
        isConnected = true;
      } catch (JSchException | IOException | InterruptedException e) {
        logger.error("Failed to connect to server", e);
      }
    }
    isConnected = false;
  }
}
