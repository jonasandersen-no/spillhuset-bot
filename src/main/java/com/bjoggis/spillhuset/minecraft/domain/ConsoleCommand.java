package com.bjoggis.spillhuset.minecraft.domain;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleCommand {

  private final Logger logger = LoggerFactory.getLogger(ConsoleCommand.class);
  private final String command;

  public ConsoleCommand(String command) {
    this.command = command;
  }

  public String execute(Session session) throws JSchException, IOException {
    logger.info("Executing command: {}", this.command);
    ChannelExec channel = (ChannelExec) session.openChannel("exec");
    channel.setCommand(this.command);
    channel.connect();
    logger.debug("Channel connected");
    String channelOutput = getChannelOutput(channel, channel.getInputStream());
    channel.disconnect();
    logger.debug("Channel disconnected");
    return channelOutput;
  }

  private String getChannelOutput(Channel channel, InputStream in) throws IOException {

    byte[] buffer = new byte[1024];
    StringBuilder strBuilder = new StringBuilder();

    String line = "";
    while (true) {
      while (in.available() > 0) {
        int i = in.read(buffer, 0, 1024);
        if (i < 0) {
          break;
        }
        strBuilder.append(new String(buffer, 0, i));
        System.out.println(line);
      }

      if (line.contains("logout")) {
        break;
      }

      if (channel.isClosed()) {
        break;
      }
      try {
        Thread.sleep(1000);
      } catch (Exception ee) {
      }
    }

    return strBuilder.toString();
  }
}
