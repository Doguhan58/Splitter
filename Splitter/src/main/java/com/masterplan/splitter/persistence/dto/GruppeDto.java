package com.masterplan.splitter.persistence.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.annotation.Id;

public record GruppeDto(@Id UUID id, String name, Set<MitgliederDto> mitglieder,
                        boolean offenFuerPersonen, boolean geschlossen, List<AusgabeDto> ausgaben,
                        Set<TransaktionDto> transaktionen) {

  public GruppeDto(UUID id, String name, Set<MitgliederDto> mitglieder, boolean offenFuerPersonen,
      boolean geschlossen, List<AusgabeDto> ausgaben, Set<TransaktionDto> transaktionen) {
    this.id = id;
    this.name = name;
    this.mitglieder = Set.copyOf(mitglieder);
    this.offenFuerPersonen = offenFuerPersonen;
    this.geschlossen = geschlossen;
    this.ausgaben = List.copyOf(ausgaben);
    this.transaktionen = Set.copyOf(transaktionen);
  }
}
