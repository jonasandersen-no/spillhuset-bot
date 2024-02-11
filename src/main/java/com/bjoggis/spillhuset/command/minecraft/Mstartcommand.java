package com.bjoggis.spillhuset.command.minecraft;

import com.bjoggis.common.discord.command.BaseCommand;
import com.bjoggis.spillhuset.Running;
import com.bjoggis.spillhuset.minecraft.CloudflareApi;
import com.bjoggis.spillhuset.minecraft.configuration.LinodeRestTemplate;
import com.bjoggis.spillhuset.minecraft.domain.ConnectionInfo;
import com.bjoggis.spillhuset.minecraft.domain.Ip;
import com.bjoggis.spillhuset.minecraft.domain.LinodeInstance;
import java.time.Duration;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class Mstartcommand extends BaseCommand {

  private final LinodeRestTemplate linodeRestTemplate;
  private final Running running;
  private final CloudflareApi cloudflareApi;

  public Mstartcommand(LinodeRestTemplate linodeRestTemplate, Running running,
      CloudflareApi cloudflareApi) {
    super("Starts a minecraft server");
    this.linodeRestTemplate = linodeRestTemplate;
    this.running = running;
    this.cloudflareApi = cloudflareApi;
  }

  @Override
  public void onSlashCommand(SlashCommandInteractionEvent event) {
    CreateResponse createResponse = create(event);
    if (createResponse == null) {
      return;
    }

    ConnectionInfo connectionInfo = new ConnectionInfo(createResponse.id(),
        Ip.from(createResponse.ip()));

    cloudflareApi.overwriteDnsRecord(Ip.from(createResponse.ip()));

    running.run(connectionInfo, event.getHook(), "classpath:start.txt", Duration.ofMinutes(3));
  }

  public CreateResponse create(SlashCommandInteractionEvent event) {

    record CreateRequest(String createdBy) {

    }

    LinodeInstance[] instances = linodeRestTemplate.get()
        .getForObject("/instance/list", LinodeInstance[].class);

    if (instances != null && instances.length > 0) {
      event.getHook().sendMessage("Server already exists").queue();
      return null;
    }

    ResponseEntity<CreateResponse> response = linodeRestTemplate.get()
        .postForEntity("/instance/create",
            new CreateRequest(event.getUser().getEffectiveName()), CreateResponse.class);
    String result = """
        Creating server with IP %s (minecraft.jonasandersen.no).
        It will take about 5 minutes before its up and running!
                
        I will respond when its ready.
        """.formatted(response.getBody().ip());
    event.getHook().sendMessage(result)
        .queue();

    return response.getBody();

  }

  public record CreateResponse(Long id, String createdBy, String label, String ip) {

  }
}
