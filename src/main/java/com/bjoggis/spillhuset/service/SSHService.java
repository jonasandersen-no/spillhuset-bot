package com.bjoggis.spillhuset.service;

import com.bjoggis.spillhuset.minecraft.configuration.LinodeRestTemplate;
import com.bjoggis.spillhuset.minecraft.domain.ConnectionInfo;
import com.bjoggis.spillhuset.minecraft.domain.ConsoleCommand;
import com.bjoggis.spillhuset.properties.SpillhusetProperties.Minecraft;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.ResourceUtils;

public class SSHService {

  private final Logger logger = LoggerFactory.getLogger(SSHService.class);

  private final Session session;

  private final LinodeRestTemplate restTemplate;

  private final ConnectionInfo connectionInfo;

  public SSHService(LinodeRestTemplate restTemplate, ConnectionInfo connectionInfo,
      Minecraft minecraft)
      throws JSchException {
    this.restTemplate = restTemplate;
    this.connectionInfo = connectionInfo;

    JSch jsch = new JSch();
    session = jsch.getSession(minecraft.username(), connectionInfo.ip().value(), 22);
    session.setPassword(minecraft.password());

    logger.info("Session created for ip {}", connectionInfo.ip());

    // Avoid asking for key confirmation
    Properties config = new Properties();
    config.put("StrictHostKeyChecking", "no");
    session.setConfig(config);
  }

  public void setupMinecraft(String commandsFile)
      throws JSchException, IOException, InterruptedException {
    if (session == null) {
      throw new IllegalStateException("Session is not set up");
    }

    attachVolume(connectionInfo.id());

    logger.info("Sleeping for 20 seconds to wait for volume to attach");
    Thread.sleep(Duration.ofSeconds(20).toMillis());

    runShellCommands(commandsFile);


  }

  @Async
  public void runShellCommands(String commandsFile) throws IOException, JSchException {
    session.connect();

    BufferedReader bufferedReader = new BufferedReader(
        new FileReader(ResourceUtils.getFile(commandsFile)));

    while (bufferedReader.ready()) {
      String command = bufferedReader.readLine();
      String result = new ConsoleCommand(command).execute(session);
      logger.info("Result: {}", result);
    }

    bufferedReader.close();
    session.disconnect();
  }

  private void attachVolume(Long linodeId) {
    restTemplate.get().patchForObject("/volume?volumeId=2034287&linodeId=" + linodeId, null,
        ResponseEntity.class);
  }
}
