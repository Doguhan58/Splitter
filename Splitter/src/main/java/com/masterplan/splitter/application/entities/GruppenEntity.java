package com.masterplan.splitter.application.entities;

import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record GruppenEntity(UUID gruppe, String name, Set<String> personen, Boolean geschlossen,
                            Set<AusgabenEntity> ausgaben) {

  public GruppenEntity(UUID gruppe, String name, Set<String> personen, Boolean geschlossen,
      Set<AusgabenEntity> ausgaben) {
    this.gruppe = gruppe;
    this.name = name;
    this.personen = Set.copyOf(personen);
    this.geschlossen = geschlossen;
    this.ausgaben = Set.copyOf(ausgaben);
  }

  public GruppenEntity(Gruppe gruppe) {
    this(
        gruppe.getId(),
        gruppe.getName(),
        Set.copyOf(
            gruppe.getMitglieder().stream().map(String::toString).collect(Collectors.toSet())),
        gruppe.isGeschlossen(),
        Set.copyOf(
            gruppe.getAusgaben().stream().map(AusgabenEntity::new).collect(Collectors.toSet()))
    );
  }

  @Override
  public Set<String> personen() {
    return Set.copyOf(personen);
  }

  @Override
  public Set<AusgabenEntity> ausgaben() {
    return Set.copyOf(ausgaben);
  }
}
