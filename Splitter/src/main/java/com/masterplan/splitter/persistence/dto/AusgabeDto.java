package com.masterplan.splitter.persistence.dto;

import java.util.Set;
import org.springframework.data.annotation.Id;

public record AusgabeDto(@Id int id, String kreditor,
                         Set<DebitorenDto> debitoren,
                         String beschreibung,
                         double kosten) {

  public AusgabeDto(int id, String kreditor, Set<DebitorenDto> debitoren, String beschreibung,
      double kosten) {
    this.id = id;
    this.kreditor = kreditor;
    this.debitoren = Set.copyOf(debitoren);
    this.beschreibung = beschreibung;
    this.kosten = kosten;
  }
}
