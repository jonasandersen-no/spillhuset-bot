package com.bjoggis.spillhuset.minecraft.configuration;

import com.bjoggis.spillhuset.configuration.IdentityAccessTokenResource;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LinodeRestTemplate implements Supplier<RestTemplate> {

  private static final String AUTH_URL = "https://login.bjoggis.com";
  public static final String API_URL = "https://api.jonasandersen.no/linode";
  private final Logger logger = LoggerFactory.getLogger(LinodeRestTemplate.class);

  @Value("${spring.security.oauth2.client.registration.spring.client-secret}")
  private String clientSecret;

  private Date expires;
  private String accessToken;

  @Override
  public RestTemplate get() {
    if (expires == null || expires.before(new Date())) {
      accessToken = getAccessToken();
    }

    return new RestTemplateBuilder()
        .defaultHeader("Authorization", "Bearer " + accessToken)
        .rootUri(API_URL)
        .build();
  }

  private String getAccessToken() {
    logger.info("Getting access token");
    RestTemplate restTemplate = new RestTemplateBuilder()
        .basicAuthentication("discord", clientSecret)
        .rootUri(AUTH_URL)
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
}
