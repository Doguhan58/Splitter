package com.masterplan.splitter.application.entities;

import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record GruppenMinEntity(UUID gruppe, java.lang.String name, Set<java.lang.String> personen) {

  public GruppenMinEntity(UUID gruppe, String name, Set<String> personen) {
    this.gruppe = gruppe;
    this.name = name;
    this.personen = Set.copyOf(personen);
  }

  public GruppenMinEntity(Gruppe gruppe) {
    this(
        gruppe.getId(),
        gruppe.getName(),
        Set.copyOf(
            gruppe.getMitglieder().stream().map(String::toString).collect(Collectors.toSet())));
  }

  @Override
  public Set<java.lang.String> personen() {
    return Set.copyOf(personen);
  }
}
