package com.bjoggis.spillhuset.minecraft;

import com.bjoggis.spillhuset.minecraft.domain.Ip;
import com.bjoggis.spillhuset.properties.SpillhusetProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CloudflareApi {

  private final Logger logger = LoggerFactory.getLogger(CloudflareApi.class);
  private final SpillhusetProperties properties;

  public CloudflareApi(SpillhusetProperties properties) {
    this.properties = properties;
  }

  @Async
  public void overwriteDnsRecord(Ip ip) {
    RestTemplate restTemplate = new RestTemplateBuilder()
        .defaultHeader("Authorization", "Bearer " + properties.cloudflare().apiKey())
        .rootUri("https://api.cloudflare.com/client/v4")
        .build();

    restTemplate.put("/zones/%s/dns_records/%s".formatted(properties.cloudflare().zoneId(),
        properties.cloudflare().dnsRecordId()), new Request(ip.value(), "minecraft",
        false, "A", "Updated by Spillhuset", List.of(), 60));

    logger.info("Overwriting DNS record for ip {}", ip.value());
  }


  record Request(String content, String name, boolean proxied, String type, String comment,
                 List<String> tags, int ttl) {

  }

}
