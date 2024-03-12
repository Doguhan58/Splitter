package com.masterplan.splitter.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.AntPathMatcher;

@Configuration
public class SecurityConfig {

  @Bean
  public WebSecurityCustomizer customizer() {
    return web -> web.ignoring().antMatchers("/api/**");
  }

  @Bean
  public SecurityFilterChain configure(HttpSecurity chainBuilder) throws Exception {
    chainBuilder
        .authorizeHttpRequests(
            configurer ->
                configurer.antMatchers("/api/**").permitAll().anyRequest().authenticated())
        .oauth2Login(Customizer.withDefaults());

    return chainBuilder.build();
  }
}
