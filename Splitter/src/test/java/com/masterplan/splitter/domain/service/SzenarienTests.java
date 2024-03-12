package com.masterplan.splitter.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.domain.aggregates.group.Transaktion;
import java.util.Set;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SzenarienTests {

  GruppenService service;

  @BeforeEach
  public void newService() {
    service = new GruppenService();
  }

  @Test
  void testSzenario1() {
    Gruppe gruppe = new Gruppe("Gruppe", "A");
    gruppe.addMitglied("B");
    gruppe.addAusgabe("A", Set.of("A", "B"), "Auslagen", Money.of(10, "EUR"));
    gruppe.addAusgabe("A", Set.of("A", "B"), "Auslagen", Money.of(20, "EUR"));
    Set<Transaktion> result = service.calculate(gruppe);
    Set<Transaktion> right = Set.of(new Transaktion("B", Money.of(15, "EUR"), "A"));
    assertThat(result).isEqualTo(right);
  }

  @Test
  void testSzenario2() {
    Gruppe gruppe = new Gruppe("Gruppe", "A");
    gruppe.addMitglied("B");
    gruppe.addAusgabe("A", Set.of("A", "B"), "Auslagen", Money.of(10, "EUR"));
    gruppe.addAusgabe("B", Set.of("A", "B"), "Auslagen", Money.of(20, "EUR"));
    Set<Transaktion> result = service.calculate(gruppe);
    Set<Transaktion> right = Set.of(new Transaktion("A", Money.of(5, "EUR"), "B"));
    assertThat(result).isEqualTo(right);
  }

  @Test
  void testSzenario3() {
    Gruppe gruppe = new Gruppe("Gruppe", "A");
    gruppe.addMitglied("B");
    gruppe.addAusgabe("A", Set.of("B"), "Auslagen", Money.of(10, "EUR"));
    gruppe.addAusgabe("A", Set.of("A", "B"), "Auslagen", Money.of(20, "EUR"));
    Set<Transaktion> result = service.calculate(gruppe);
    Set<Transaktion> right = Set.of(new Transaktion("B", Money.of(20, "EUR"), "A"));
    assertThat(result).isEqualTo(right);
  }

  @Test
  void testSzenario4() {
    Gruppe gruppe = new Gruppe("Gruppe", "A");
    gruppe.addMitglied("B");
    gruppe.addMitglied("C");
    gruppe.addAusgabe("A", Set.of("A", "B"), "Auslagen", Money.of(10, "EUR"));
    gruppe.addAusgabe("B", Set.of("B", "C"), "Auslagen", Money.of(10, "EUR"));
    gruppe.addAusgabe("C", Set.of("A", "C"), "Auslagen", Money.of(10, "EUR"));
    Set<Transaktion> result = service.calculate(gruppe);
    assertThat(result).isEmpty();
  }

  @Test
  void testSzenario5() {
    Gruppe gruppe = new Gruppe("Gruppe", "Anton");
    gruppe.addMitglied("Berta");
    gruppe.addMitglied("Christian");
    gruppe.addAusgabe("Anton", Set.of("Anton", "Berta", "Christian"), "Auslagen",
        Money.of(60, "EUR"));
    gruppe.addAusgabe("Berta", Set.of("Anton", "Berta", "Christian"), "Auslagen2", Money.of(30,
        "EUR"));
    gruppe.addAusgabe("Christian", Set.of("Berta", "Christian"), "Auslagen3", Money.of(100, "EUR"));
    Set<Transaktion> result = service.calculate(gruppe);
    Set<Transaktion> right = Set.of(
        new Transaktion("Berta", Money.of(30, "EUR"), "Anton"),
        new Transaktion("Berta", Money.of(20, "EUR"), "Christian"));
    assertThat(result).isEqualTo(right);
  }

  @Test
  void testSzenario6() {
    Gruppe gruppe = new Gruppe("Gruppe", "A");
    gruppe.addMitglied("B");
    gruppe.addMitglied("C");
    gruppe.addMitglied("D");
    gruppe.addMitglied("E");
    gruppe.addMitglied("F");
    gruppe.addAusgabe("A", Set.of("A", "B", "C", "D", "E", "F"), "Hotelzimmer",
        Money.of(564, "EUR"));
    gruppe.addAusgabe("B", Set.of("B", "A"), "Benzin(Hinweg)", Money.of(38.58, "EUR"));
    gruppe.addAusgabe("B", Set.of("B", "A", "D"), "Benzin(Rückweg)", Money.of(38.58, "EUR"));
    gruppe.addAusgabe("C", Set.of("C", "E", "F"), "Benzin", Money.of(82.11, "EUR"));
    gruppe.addAusgabe("D", Set.of("A", "B", "C", "D", "E", "F"), "Staedtetour",
        Money.of(96, "EUR"));
    gruppe.addAusgabe("F", Set.of("B", "E", "F"), "Theatervorstellung", Money.of(95.37, "EUR"));
    Set<Transaktion> result = service.calculate(gruppe);
    Set<Transaktion> right = Set.of(
        new Transaktion("B", Money.of(96.78, "EUR"), "A"),
        new Transaktion("C", Money.of(55.26, "EUR"), "A"),
        new Transaktion("D", Money.of(26.86, "EUR"), "A"),
        new Transaktion("E", Money.of(169.16, "EUR"), "A"),
        new Transaktion("F", Money.of(73.79, "EUR"), "A"));
    assertThat(result).isEqualTo(right);
  }

  @Test
  void testSzenario7() {
    Gruppe gruppe = new Gruppe("Gruppe", "A");
    gruppe.addMitglied("B");
    gruppe.addMitglied("C");
    gruppe.addMitglied("D");
    gruppe.addMitglied("E");
    gruppe.addMitglied("F");
    gruppe.addMitglied("G");
    gruppe.addAusgabe("D", Set.of("D", "F"), "Kino", Money.of(20, "EUR"));
    gruppe.addAusgabe("G", Set.of("B"), "Essen", Money.of(10, "EUR"));
    gruppe.addAusgabe("E", Set.of("A", "C", "E"), "Theater", Money.of(75, "EUR"));
    gruppe.addAusgabe("F", Set.of("A", "F"), "Bier", Money.of(50, "EUR"));
    gruppe.addAusgabe("E", Set.of("D"), "Wein", Money.of(40, "EUR"));
    gruppe.addAusgabe("F", Set.of("B", "F"), "Vodka", Money.of(40, "EUR"));
    gruppe.addAusgabe("F", Set.of("C"), "Whisky", Money.of(5, "EUR"));
    gruppe.addAusgabe("G", Set.of("A"), "A", Money.of(30, "EUR"));
    Set<Transaktion> result = service.calculate(gruppe);
    Set<Transaktion> right = Set.of(
        new Transaktion("A", Money.of(40, "EUR"), "F"),
        new Transaktion("A", Money.of(40, "EUR"), "G"),
        new Transaktion("B", Money.of(30, "EUR"), "E"),
        new Transaktion("C", Money.of(30, "EUR"), "E"),
        new Transaktion("D", Money.of(30, "EUR"), "E")
    );
    assertThat(result).isEqualTo(right);
  }

}
