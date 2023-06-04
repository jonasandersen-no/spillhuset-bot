package com.bjoggis.spillhuset.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize ->
            authorize.requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/v1/**").authenticated())
        .oauth2ResourceServer(oauth2 -> {
          oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthenticationConverter()));
        }).csrf(csrf -> csrf.disable());
    return http.build();
  }
}
