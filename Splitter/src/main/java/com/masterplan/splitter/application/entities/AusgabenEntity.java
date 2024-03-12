package com.masterplan.splitter.application.entities;

import com.masterplan.splitter.domain.aggregates.group.Ausgabe;
import java.util.Set;
import java.util.stream.Collectors;

public record AusgabenEntity(String grund, String glaeubiger, Long cent, Set<String> schuldner) {

  public AusgabenEntity(
      String grund, String glaeubiger, Long cent, Set<String> schuldner) {
    this.grund = grund;
    this.glaeubiger = glaeubiger;
    this.cent = cent;
    this.schuldner = Set.copyOf(schuldner);
  }

  public AusgabenEntity(Ausgabe ausgabe) {
    this(
        ausgabe.beschreibung(),
        ausgabe.kreditor(),
        ausgabe.kosten().scaleByPowerOfTen(2).getNumber().longValueExact(),
        Set.copyOf(ausgabe.debitoren().stream().map(String::toString).collect(Collectors.toSet()))
    );
  }

  @Override
  public Set<String> schuldner() {
    return Set.copyOf(schuldner);
  }
}
