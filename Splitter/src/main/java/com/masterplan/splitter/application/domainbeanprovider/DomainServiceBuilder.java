package com.masterplan.splitter.application.domainbeanprovider;

import com.masterplan.splitter.domain.service.GruppenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceBuilder {

  @Bean
  public GruppenService getNewGroupService() {
    return new GruppenService();
  }
}
