package com.bjoggis.spillhuset.command.minecraft;

import com.bjoggis.common.discord.command.BaseCommand;
import com.bjoggis.spillhuset.minecraft.domain.LinodeInstance;
import com.bjoggis.spillhuset.minecraft.configuration.LinodeRestTemplate;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MstopCommand extends BaseCommand {

  private final LinodeRestTemplate linodeRestTemplate;

  public MstopCommand(LinodeRestTemplate linodeRestTemplate) {
    super("Stops the minecraft server");
    this.linodeRestTemplate = linodeRestTemplate;
  }

  @Override
  public void onSlashCommand(SlashCommandInteractionEvent event) {
    RestTemplate restTemplate = linodeRestTemplate.get();

    LinodeInstance[] instances = restTemplate.getForObject("/instance/list", LinodeInstance[].class);

    if (instances == null || instances.length == 0) {
      event.getHook().sendMessage("Server does not exist").queue();
      return;
    }

    for (LinodeInstance instance : instances) {
      restTemplate.delete("/instance/%d".formatted(instance.id()));
    }
    event.getHook().sendMessage("Deleted server").queue();

  }
}
