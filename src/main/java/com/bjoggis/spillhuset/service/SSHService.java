package com.bjoggis.spillhuset.service;

import com.bjoggis.spillhuset.command.minecraft.Mstartcommand.CreateLResponse;
import com.bjoggis.spillhuset.minecraft.configuration.LinodeRestTemplate;
import com.bjoggis.spillhuset.minecraft.domain.ConsoleCommand;
import com.bjoggis.spillhuset.properties.SpillhusetProperties.Minecraft;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

public class SSHService {

  private final Logger logger = LoggerFactory.getLogger(SSHService.class);

  private final Session session;

  private final LinodeRestTemplate restTemplate;
  private final InteractionHook hook;

  private final CreateLResponse createResponse;

  public SSHService(LinodeRestTemplate restTemplate, CreateLResponse createResponse, InteractionHook hook,
      Minecraft minecraft)
      throws JSchException {
    this.restTemplate = restTemplate;
    this.createResponse = createResponse;
    this.hook = hook;

    JSch jsch = new JSch();
    session = jsch.getSession(minecraft.username(), createResponse.ip(), 22);
    session.setPassword(minecraft.password());

    logger.info("Session created for ip {}", createResponse.ip());

    // Avoid asking for key confirmation
    Properties config = new Properties();
    config.put("StrictHostKeyChecking", "no");
    session.setConfig(config);
  }

  public void setupMinecraft() throws JSchException, IOException, InterruptedException {
    if (session == null) {
      throw new IllegalStateException("Session is not set up");
    }

    attachVolume(createResponse.id());

    logger.info("Sleeping for 20 seconds to wait for volume to attach");
    Thread.sleep(Duration.ofSeconds(20).toMillis());

    session.connect();

    logger.info("Result: {}",
        new ConsoleCommand("curl -fsSL https://get.docker.com -o get-docker.sh").execute(session));

    logger.info("Result: {}", new ConsoleCommand("sudo sh ./get-docker.sh").execute(session));

    logger.info("Result: {}", new ConsoleCommand(
        "curl -fsSL https://raw.githubusercontent.com/NotBjoggisAtAll/minecraft-compose/main/docker-compose.yml -o docker-compose.yml").execute(
        session));

    logger.info("Result: {}",
        new ConsoleCommand("mkdir \"/mnt/minecraft-volume-01\"").execute(session));

    logger.info("Result: {}", new ConsoleCommand(
        "mount \"/dev/disk/by-id/scsi-0Linode_Volume_minecraft-volume-01\" \"/mnt/minecraft-volume-01\"").execute(
        session));

    logger.info("Result: {}", new ConsoleCommand("docker compose up -d").execute(session));

    logger.info("Done setting up server");

    Thread.sleep(Duration.ofSeconds(5).toMillis());
    hook.sendMessage("Server should now be ready!").queue();

    session.disconnect();
  }

  private void attachVolume(Long linodeId) {
    restTemplate.get().patchForObject("/volume?volumeId=2034287&linodeId=" + linodeId, null,
        ResponseEntity.class);
  }
}
