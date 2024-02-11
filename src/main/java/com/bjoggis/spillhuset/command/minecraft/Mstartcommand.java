package com.bjoggis.spillhuset.command.minecraft;

import com.bjoggis.common.discord.command.BaseCommand;
import com.bjoggis.spillhuset.Running;
import com.bjoggis.spillhuset.minecraft.configuration.LinodeRestTemplate;
import com.bjoggis.spillhuset.minecraft.domain.LinodeInstance;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jilt.Builder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class Mstartcommand extends BaseCommand {

  private final LinodeRestTemplate linodeRestTemplate;
  private final Running running;

  public Mstartcommand(LinodeRestTemplate linodeRestTemplate, Running running) {
    super("Starts a minecraft server");
    this.linodeRestTemplate = linodeRestTemplate;
    this.running = running;
  }

  @Override
  public void onSlashCommand(SlashCommandInteractionEvent event) {
    CreateLResponse createLResponse = create(event);
    if (createLResponse == null) {
      return;
    }
    running.run(createLResponse, event.getHook());

  }

  public CreateLResponse create(SlashCommandInteractionEvent event) {

    record CreateRequest(String createdBy) {

    }

    LinodeInstance[] instances = linodeRestTemplate.get()
        .getForObject("/instance/list", LinodeInstance[].class);

    if (instances != null && instances.length > 0) {
      event.getHook().sendMessage("Server already exists").queue();
      return null;
    }

    ResponseEntity<CreateLResponse> response = linodeRestTemplate.get()
        .postForEntity("/instance/create",
            new CreateRequest(event.getUser().getEffectiveName()), CreateLResponse.class);
    String result = """
        Creating server with IP %s.
        It will take about 5 minutes before its up and running!
                
        I will respond when its ready.
        """.formatted(response.getBody().ip());
    event.getHook().sendMessage(result)
        .queue();

    return response.getBody();

  }

  @Builder
  public record CreateLResponse(Long id, String createdBy, String label, String ip) {

  }
}
