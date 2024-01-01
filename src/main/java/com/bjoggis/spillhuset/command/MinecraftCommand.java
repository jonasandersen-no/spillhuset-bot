package com.bjoggis.spillhuset.command;

import com.bjoggis.common.discord.command.BaseCommand;
import com.bjoggis.common.discord.model.Option;
import com.bjoggis.spillhuset.configuration.IdentityAccessTokenResource;
import java.util.Calendar;
import java.util.Date;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MinecraftCommand extends BaseCommand {

  private final Logger logger = LoggerFactory.getLogger(MinecraftCommand.class);

  @Value("${spring.security.oauth2.client.registration.spring.client-secret}")
  private String clientSecret;

  private Date expires;
  private String accessToken;

  public MinecraftCommand() {
    super("Setup the server");
    addOption(new Option("command", "create,info,delete", OptionType.STRING, false, false));

  }

  @Override
  public void onSlashCommand(SlashCommandInteractionEvent event) {
    try {
      OptionMapping command = event.getOption("command");
      String value = command.getAsString();
      if ("create".equalsIgnoreCase(value)) {
        create(event);
      } else if ("delete".equalsIgnoreCase(value)) {
        delete(event);
      } else if ("info".equalsIgnoreCase(value)) {
        getInfo(event);
      } else {
        event.getHook().sendMessage("Unknown command").queue();
      }
    } catch (Exception e) {
      logger.error("Error", e);
      event.getHook().sendMessage("Something went wrong").queue();
    }
  }

  private void create(SlashCommandInteractionEvent event) {
    RestTemplate restTemplate = setupRestTemplate();

    record CreateRequest(String createdBy) {

    }

    record CreateResponse(String createdBy, String label, String ip) {

    }

    Instance[] instances = restTemplate.getForObject("/instance/list", Instance[].class);

    if (instances != null && instances.length > 0) {
      event.getHook().sendMessage("Server already exists").queue();
      return;
    }

    ResponseEntity<CreateResponse> response = restTemplate.postForEntity("/instance/create",
        new CreateRequest(event.getUser().getEffectiveName()), CreateResponse.class);
    event.getHook().sendMessage("Created server with ip %s".formatted(response.getBody().ip()))
        .queue();

  }

  private void delete(SlashCommandInteractionEvent event) {
    RestTemplate restTemplate = setupRestTemplate();

    Instance[] instances = restTemplate.getForObject("/instance/list", Instance[].class);

    if (instances == null || instances.length == 0) {
      event.getHook().sendMessage("Server does not exist").queue();
      return;
    }

    for (Instance instance : instances) {
      restTemplate.delete("/instance/%d".formatted(instance.id()));
    }
    event.getHook().sendMessage("Deleted server").queue();
  }


  private void getInfo(SlashCommandInteractionEvent event) {
    RestTemplate restTemplate = setupRestTemplate();

    Instance[] instances = restTemplate.getForObject("/instance/list", Instance[].class);

    if (instances == null || instances.length == 0) {
      event.getHook().sendMessage("Server does not exist").queue();
      return;
    }

    if (instances.length > 1) {
      logger.warn("More than one server exists, this should not happen");
    }

    if (instances.length == 1) {
      Instance instance = instances[0];
      event.getHook().sendMessage("Server info:\nLabel: %s\nIP: %s\nStatus: %s\nCreated: %s"
              .formatted(instance.label(), instance.ip(), instance.status(), instance.created()))
          .queue();
      return;
    }

    StringBuilder builder = new StringBuilder();
    for (Instance instance : instances) {
      builder.append(instance.label()).append(" ").append(instance.ip()).append("\n");
    }

    event.getHook().sendMessage("List of instances:\n%s".formatted(builder.toString())).queue();
  }

  private RestTemplate setupRestTemplate() {
    if (expires == null || expires.before(new Date())) {
      accessToken = getAccessToken();
    }

    RestTemplate restTemplate = new RestTemplateBuilder()
        .defaultHeader("Authorization", "Bearer " + accessToken)
        .rootUri("https://api.jonasandersen.no/linode")
        .build();
    return restTemplate;
  }

  private String getAccessToken() {
    logger.info("Getting access token");
    RestTemplate restTemplate = new RestTemplateBuilder()
        .basicAuthentication("discord", clientSecret)
        .rootUri("https://login.bjoggis.com")
        .build();

    IdentityAccessTokenResource resource = null;
    try {
      resource = restTemplate.postForObject(
          "/oauth2/token?grant_type=client_credentials", null, IdentityAccessTokenResource.class);
    } catch (Exception e) {
      logger.info("Error getting access token", e);
    }
    Long expiresInSeconds = resource.getExpiresIn();
    Calendar instance = Calendar.getInstance();

    instance.add(Calendar.SECOND, expiresInSeconds.intValue());

    expires = instance.getTime();
    return resource.getAccessToken();
  }

  record Instance(Long id, String label, String ip, String status, String created) {

  }
}
