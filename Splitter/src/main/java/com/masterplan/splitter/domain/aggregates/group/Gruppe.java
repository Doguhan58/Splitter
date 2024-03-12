package com.masterplan.splitter.domain.aggregates.group;

import com.masterplan.splitter.annotations.AggregateRoot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.javamoney.moneta.Money;

@AggregateRoot
public class Gruppe {

  private final UUID id;
  private final String name;
  private final Set<String> mitglieder;
  private boolean offenFuerPersonen = true;
  private boolean geschlossen = false;
  private final List<Ausgabe> ausgaben;
  private Set<Transaktion> transaktionen;

  public Gruppe(String name, String creator) {
    this.name = name;
    this.id = null;
    this.mitglieder = new HashSet<>();
    mitglieder.add(creator);
    ausgaben = new ArrayList<>();
    transaktionen = new HashSet<>();
  }

  public Gruppe(UUID id, String name, Set<String> mitglieder,
      boolean offenFuerPersonen, boolean geschlossen, List<Ausgabe> ausgaben,
      Set<Transaktion> transaktionen) {
    this.name = name;
    this.id = id;
    this.mitglieder = new HashSet<>(Set.copyOf(mitglieder));
    this.offenFuerPersonen = offenFuerPersonen;
    this.geschlossen = geschlossen;
    this.ausgaben = new ArrayList<>(List.copyOf(ausgaben));
    this.transaktionen = new HashSet<>(Set.copyOf(transaktionen));
  }

  public void addMitglied(String name) {
    mitglieder.add(name);
  }

  public void addAusgabe(
      String kreditor, Set<String> debitoren, String beschreibung,
      Money kosten) {
    ausgaben.add(
        new Ausgabe(kreditor,
            debitoren.stream().map(String::new).collect(Collectors.toSet()),
            beschreibung,
            kosten));
  }

  public void schliessenFuerPersonen() {
    offenFuerPersonen = false;
  }

  public void schliessen() {
    geschlossen = true;
  }

  public String getName() {
    return name;
  }

  public boolean isOffenFuerPersonen() {
    return offenFuerPersonen;
  }

  public boolean isGeschlossen() {
    return geschlossen;
  }

  public Set<String> getMitglieder() {
    return Set.copyOf(mitglieder);
  }

  public List<Ausgabe> getAusgaben() {
    return List.copyOf(ausgaben);
  }

  public Set<Transaktion> getTransaktionen() {
    return Set.copyOf(transaktionen);
  }

  public boolean hasMitglied(String name) {
    return mitglieder.stream().anyMatch(handle -> handle.equals(name));
  }

  public void setTransaktionen(
      Set<Transaktion> transaktionen) {
    this.transaktionen = Set.copyOf(transaktionen);
  }

  public UUID getId() {
    return id;
  }

  @Override
  public String toString() {
    return "Gruppe{"
        + "id=" + id
        + ", name='" + name + '\''
        + ", mitglieder=" + mitglieder
        + ", offenfuerpersonen=" + offenFuerPersonen
        + ", geschlossen=" + geschlossen
        + ", ausgaben=" + ausgaben
        + ", transaktionen=" + transaktionen
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Gruppe gruppe = (Gruppe) o;
    return offenFuerPersonen == gruppe.offenFuerPersonen && geschlossen == gruppe.geschlossen
        && Objects.equals(id, gruppe.id) && Objects.equals(name, gruppe.name)
        && Objects.equals(mitglieder, gruppe.mitglieder) && Objects.equals(
        ausgaben, gruppe.ausgaben) && Objects.equals(transaktionen, gruppe.transaktionen);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, mitglieder, offenFuerPersonen, geschlossen, ausgaben,
        transaktionen);
  }
}
