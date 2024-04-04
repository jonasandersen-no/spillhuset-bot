package com.bjoggis.spillhuset.minecraft.configuration;

import com.bjoggis.spillhuset.configuration.IdentityAccessTokenResource;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class LinodeRestTemplate implements Supplier<RestTemplate> {

  private static final String AUTH_URL = "https://auth.jonasandersen.no";
  public static final String API_URL = "https://api.jonasandersen.no/linode";
  private final Logger logger = LoggerFactory.getLogger(LinodeRestTemplate.class);

  @Value("${spillhuset.linode.client-id}")
  private String clientId;

  @Value("${spillhuset.linode.client-secret}")
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
        .basicAuthentication(clientId, clientSecret)
        .rootUri(AUTH_URL)
        .build();

    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("grant_type", "client_credentials");

    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/x-www-form-urlencoded");
    HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<IdentityAccessTokenResource> resource = null;
    try {

      resource = restTemplate.exchange("/oauth2/token", HttpMethod.POST, requestEntity, IdentityAccessTokenResource.class);
    } catch (Exception e) {
      logger.info("Error getting access token", e);
    }
    Long expiresInSeconds = resource.getBody().getExpiresIn();
    Calendar instance = Calendar.getInstance();

    instance.add(Calendar.SECOND, expiresInSeconds.intValue());

    expires = instance.getTime();
    return resource.getBody().getAccessToken();
  }
}
