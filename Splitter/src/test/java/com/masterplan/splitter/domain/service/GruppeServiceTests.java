package com.masterplan.splitter.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.masterplan.splitter.domain.aggregates.group.Ausgabe;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.domain.aggregates.group.Transaktion;
import com.masterplan.splitter.exceptions.GruppeGeschlossenException;
import com.masterplan.splitter.exceptions.PersonKeinMitgliedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GruppeServiceTests {

  GruppenService service;

  @BeforeEach
  public void newService() {
    service = new GruppenService();
  }

  @Test
  @DisplayName("getDebts gibt richtiges Ergebnis für: Nur ein Debitor")
  void test1() {
    Gruppe gruppe1 = new Gruppe("coole gruppe", "A");
    gruppe1.addMitglied("B");
    gruppe1.addMitglied("C");
    gruppe1.addAusgabe("A", Set.of("A", "B", "C"), "Döner", Money.of(60, "EUR"));
    gruppe1.addAusgabe("B", Set.of("A", "B", "C"), "Kino", Money.of(30, "EUR"));
    gruppe1.addAusgabe("C", Set.of("C", "B"), "Bar", Money.of(100, "EUR"));
    Map<String, Money> debts = service.getDebts(gruppe1);
    Map<String, Money> right = Map.of(
        "A", Money.of(30, "EUR"),
        "B", Money.of(-50, "EUR"),
        "C", Money.of(20, "EUR"));
    assertThat(debts).isEqualTo(right);
  }

  @Test
  @DisplayName("getDebts gibt richtige Ergebnis für Beispiel aus Aufgabenstellung")
  void test2() {
    Gruppe gruppe1 = new Gruppe("coole gruppe", "A");
    gruppe1.addMitglied("B");
    gruppe1.addMitglied("C");
    gruppe1.addMitglied("D");
    gruppe1.addMitglied("E");
    gruppe1.addMitglied("F");
    gruppe1.addAusgabe("A", Set.of("A", "B", "C", "D", "E", "F"), "Hotel", Money.of(564, "EUR"));
    gruppe1.addAusgabe("B", Set.of("A", "B"), "Hinfahrt B", Money.of(38.58, "EUR"));
    gruppe1.addAusgabe("B", Set.of("A", "B", "D"), "Rückfahrt B", Money.of(38.58, "EUR"));
    gruppe1.addAusgabe("C", Set.of("C", "E", "F"), "Fahrt C", Money.of(82.11, "EUR"));
    gruppe1.addAusgabe("D", Set.of("A", "B", "C", "D", "E", "F"), "Staedtetour",
        Money.of(96, "EUR"));
    gruppe1.addAusgabe("F", Set.of("B", "E", "F"), "Theater", Money.of(95.37, "EUR"));
    Map<String, Money> debts = service.getDebts(gruppe1);
    Map<String, Money> right = Map.of(
        "A", Money.of(421.85, "EUR"),
        "B", Money.of(-96.78, "EUR"),
        "C", Money.of(-55.26, "EUR"),
        "D", Money.of(-26.86, "EUR"),
        "E", Money.of(-169.16, "EUR"),
        "F", Money.of(-73.79, "EUR"));
    assertThat(debts).isEqualTo(right);
  }

  @Test
  @DisplayName("calculate gibt Ergebnis für Beispiel aus Aufgabenstellung")
  void test3() {
    Gruppe gruppe1 = new Gruppe("coole gruppe", "A");
    gruppe1.addMitglied("B");
    gruppe1.addMitglied("C");
    gruppe1.addMitglied("D");
    gruppe1.addMitglied("E");
    gruppe1.addMitglied("F");
    gruppe1.addAusgabe("A", Set.of("A", "B", "C", "D", "E", "F"), "Hotel", Money.of(564, "EUR"));
    gruppe1.addAusgabe("B", Set.of("A", "B"), "Hinfahrt B", Money.of(38.58, "EUR"));
    gruppe1.addAusgabe("B", Set.of("A", "B", "D"), "Rückfahrt B", Money.of(38.58, "EUR"));
    gruppe1.addAusgabe("C", Set.of("C", "E", "F"), "Fahrt C", Money.of(82.11, "EUR"));
    gruppe1.addAusgabe("D", Set.of("A", "B", "C", "D", "E", "F"), "Staedtetour",
        Money.of(96, "EUR"));
    gruppe1.addAusgabe("F", Set.of("B", "E", "F"), "Theater", Money.of(95.37, "EUR"));
    Set<Transaktion> beispiel = Set.of(
        new Transaktion("B", Money.of(96.78, "EUR"), "A"),
        new Transaktion("E", Money.of(169.16, "EUR"), "A"),
        new Transaktion("F", Money.of(73.79, "EUR"), "A"),
        new Transaktion("C", Money.of(55.26, "EUR"), "A"),
        new Transaktion("D", Money.of(26.86, "EUR"), "A")
    );
    assertThat(beispiel).isEqualTo(service.calculate(gruppe1));
  }

  @Test
  @DisplayName("calculate gibt Ergebnis für Beispiel aus den Folien")
  void test4() {
    Gruppe gruppe1 = new Gruppe("coole gruppe", "A");
    gruppe1.addMitglied("B");
    gruppe1.addMitglied("C");
    gruppe1.addMitglied("D");
    gruppe1.addAusgabe("A", Set.of("A", "B", "C"), "Döner", Money.of(60, "EUR"));
    gruppe1.addAusgabe("B", Set.of("A", "B", "C"), "Kino", Money.of(30, "EUR"));
    gruppe1.addAusgabe("C", Set.of("C", "B"), "Bar", Money.of(100, "EUR"));
    Set<Transaktion> beispiel = Set.of(
        new Transaktion("B", Money.of(30, "EUR"), "A"),
        new Transaktion("B", Money.of(20, "EUR"), "C")

    );
    assertThat(service.calculate(gruppe1)).isEqualTo(beispiel);
  }

  @Test
  @DisplayName("easyKnapsack mit gleichen Positivbeträgen und Negativbeträgen")
  void test5() {
    List<String> nameList = new ArrayList<>(List.of("A", "B", "C", "D", "E", "F"));
    List<Money> moneyList = new ArrayList<>(List.of(
        Money.of(376, "EUR"),
        Money.of(376, "EUR"),
        Money.of(-188, "EUR"),
        Money.of(-188, "EUR"),
        Money.of(-188, "EUR"),
        Money.of(-188, "EUR")));
    Set<Transaktion> result = service.easyKnapsack(nameList, moneyList);
    Set<Transaktion> right = Set.of(
        new Transaktion("D", Money.of(188, "EUR"), "A"),
        new Transaktion("E", Money.of(188, "EUR"), "A"),
        new Transaktion("F", Money.of(188, "EUR"), "B"),
        new Transaktion("C", Money.of(188, "EUR"), "B")
    );
    assertThat(result).isEqualTo(right);
  }

  @Test
  @DisplayName("easyKnapsack mit zwei Kreditoren")
  void test6() {
    List<String> nameList = new ArrayList<>(List.of("A", "B", "C", "D"));
    List<Money> moneyList = new ArrayList<>(List.of(
        Money.of(30, "EUR"),
        Money.of(20, "EUR"),
        Money.of(-10, "EUR"),
        Money.of(-40, "EUR")));
    Set<Transaktion> result = service.easyKnapsack(nameList, moneyList);
    Set<Transaktion> right = Set.of(
        new Transaktion("C", Money.of(10, "EUR"), "A"),
        new Transaktion("D", Money.of(20, "EUR"), "A"),
        new Transaktion("D", Money.of(20, "EUR"), "B")
    );
    assertThat(result).isEqualTo(right);
  }

  @Test
  @DisplayName("Methode mapToListInOrderValues gibt Geldbeträge Rückwärts aus: ein Debitor")
  void test7() {
    Map<String, Money> setup = Map.of(
        "A", Money.of(30, "EUR"),
        "B", Money.of(-50, "EUR"),
        "C", Money.of(20, "EUR"));
    List<Money> result = service.mapToListInOrderValues(setup);
    List<Money> right = List.of(
        Money.of(30, "EUR"),
        Money.of(20, "EUR"),
        Money.of(-50, "EUR"));
    assertThat(result).isEqualTo(right);
  }

  @Test
  @DisplayName("Methode mapToListInOrderValues gibt Geldbeträge Rückwärts")
  void test8() {
    Map<String, Money> setup = Map.of(
        "A", Money.of(421.85, "EUR"),
        "B", Money.of(-96.78, "EUR"),
        "C", Money.of(-55.26, "EUR"),
        "D", Money.of(-26.86, "EUR"),
        "E", Money.of(-169.16, "EUR"),
        "F", Money.of(-73.79, "EUR"));
    List<Money> result = service.mapToListInOrderValues(setup);
    List<Money> right = List.of(
        Money.of(421.85, "EUR"),
        Money.of(-26.86, "EUR"),
        Money.of(-55.26, "EUR"),
        Money.of(-73.79, "EUR"),
        Money.of(-96.78, "EUR"),
        Money.of(-169.16, "EUR"));
    assertThat(result).isEqualTo(right);
  }

  @Test
  @DisplayName("Methode mapToListInOrderKeys gibt Namen Rückwärts aus")
  void test9() {
    Map<java.lang.String, Money> setup = Map.of(
        "A", Money.of(30, "EUR"),
        "B", Money.of(-50, "EUR"),
        "C", Money.of(20, "EUR"));
    List<String> result = service.mapToListInOrderKeys(setup);
    List<String> right = List.of("A", "C", "B");
    assertThat(result).isEqualTo(right);
  }

  @Test
  @DisplayName("Methode mapToListInOrderKeys gibt Namen Rückwärts aus")
  void test10() {
    Map<java.lang.String, Money> setup = Map.of(
        "A", Money.of(421.85, "EUR"),
        "B", Money.of(-96.78, "EUR"),
        "C", Money.of(-55.26, "EUR"),
        "D", Money.of(-26.86, "EUR"),
        "E", Money.of(-169.16, "EUR"),
        "F", Money.of(-73.79, "EUR"));
    List<String> result = service.mapToListInOrderKeys(setup);
    List<String> right = List.of("A", "D", "C", "F", "B", "E");
    assertThat(result).isEqualTo(right);
  }

  @Test
  @DisplayName("Service fügt in bestehende Gruppe ein")
  void test11() {
    Gruppe gruppe = new Gruppe("Gruppe", "A");
    service.addMitglied(gruppe, "testUser");
    assertThat(gruppe.getMitglieder()).containsExactlyInAnyOrder("A", "testUser");
  }

  @Test
  @DisplayName("addGruppenAusgabe fügt genau einmal die richtige Ausgabe hinzu")
  void test12() throws GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = new Gruppe("ABC", "A");
    gruppe.addMitglied("B");
    gruppe.addMitglied("C");
    service.addGruppenAusgabe(gruppe, "A", Set.of("A", "B", "C"), "Essen", Money.of(20, "EUR"));
    assertThat(gruppe.getAusgaben()).containsExactly(
        new Ausgabe(
            "A",
            Set.of("A", "B", "C"),
            "Essen",
            Money.of(20, "EUR")));
  }

  @Test
  @DisplayName("addGruppenAusgabe Elemente sind einfügbar")
  void test13() throws GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = new Gruppe("ABC", "A");
    gruppe.addMitglied("B");
    gruppe.addMitglied("C");
    service.addGruppenAusgabe(gruppe, "A", Set.of("A", "B", "C"), "Döner", Money.of(20, "EUR"));
    assertThat(gruppe.getAusgaben().get(0)).isInstanceOf(Ausgabe.class);
  }

  @Test
  @DisplayName("Gruppen können über Service erzeugt werden")
  void test14() {
    Gruppe gruppe = service.createGruppe("Simon's Bande", "Simon");
    assertThat(gruppe).isInstanceOf(Gruppe.class);
  }

  @Test
  @DisplayName("Wenn Transaktion in Gruppe gemacht ist die gruppe Geschlossen")
  void test151() throws GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = new Gruppe("Simon's Bande", "Simon");
    gruppe.addMitglied("rafa");
    service.addGruppenAusgabe(gruppe, "Simon", Set.of("Simon", "rafa"), "Wette",
        Money.of(10, "EUR"));
    assertThat(gruppe.isOffenFuerPersonen()).isFalse();
  }

  @Test
  @DisplayName("in eine geschlossene Gruppe lassen sich keine Mitglieder mehr hinzufügen")
  void test152() {
    Gruppe gruppe = new Gruppe("Simon's Bande", "Simon");
    gruppe.schliessenFuerPersonen();
    service.addMitglied(gruppe, "dodogan");
    assertThat(gruppe.getMitglieder().size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Gruppe hat Transaktion gemacht ist damit geschlossen")
  void test16() throws GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = new Gruppe("Simon's Bande", "Simon");
    gruppe.addMitglied("rafa");
    service.addGruppenAusgabe(gruppe, "Simon", Set.of("Simon", "rafa"), "Wette",
        Money.of(10, "EUR"));
    assertThat(gruppe.isOffenFuerPersonen()).isFalse();
  }

  @Test
  @DisplayName("In fertige Gruppe lassen sich keine Ausgaben mehr hinzufügen")
  void test17() {
    Gruppe gruppe = new Gruppe("Simon's Bande", "Simon");
    gruppe.addMitglied("rafa");
    gruppe.schliessen();
    GruppeGeschlossenException thrown = assertThrows(
        GruppeGeschlossenException.class,
        () -> service.addGruppenAusgabe(gruppe, "Simon", Set.of("Simon", "rafa"), "Wette",
            Money.of(10, "EUR")),
        "did not throw"
    );
    assertTrue(thrown.getMessage().contentEquals("Gruppe ist bereits geschlossen"));
  }

  @Test
  @DisplayName("In fertige Gruppe lassen sich keine Mitglieder mehr hinzufügen")
  void test18() {
    Gruppe gruppe = new Gruppe("Simon's Bande", "Simon");
    gruppe.addMitglied("Rafa");
    gruppe.schliessen();
    service.addMitglied(gruppe, "Pedro");
    assertThat(gruppe.getMitglieder().size()).isEqualTo(2);
  }

  @Test
  @DisplayName("finishGroup finished die Gruppe")
  void test19() {
    Gruppe gruppe = new Gruppe("Simon's Bande", "Simon");
    service.gruppeSchliessen(gruppe);
    assertThat(gruppe.isGeschlossen()).isTrue();
  }

  @Test
  @DisplayName("getGroupExpenses behält die Einfügereihenfolge bei")
  void test20() throws GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = new Gruppe("Simon's Bande", "Simon");
    gruppe.addMitglied("rafa");
    service.addGruppenAusgabe(gruppe, "Simon", Set.of("Simon", "rafa"), "Wette",
        Money.of(10, "EUR"));
    service.addGruppenAusgabe(gruppe, "Simon", Set.of("Simon", "rafa"), "wett",
        Money.of(1000, "EUR"));
    List<Ausgabe> right = List.of(
        new Ausgabe("Simon", Set.of("Simon", "rafa"), "Wette",
            Money.of(10, "EUR")),
        new Ausgabe("Simon", Set.of("Simon", "rafa"), "wett",
            Money.of(1000, "EUR")));
    assertThat(service.getAusgabe(gruppe)).isEqualTo(right);
  }

  @Test
  @DisplayName("hasMember Methode funktioniert")
  void test21() {
    Gruppe gruppe = new Gruppe("Simon's Bande", "Simon");
    assertThat(service.hasMitglied(gruppe, "Simon")).isTrue();
  }

  @Test
  @DisplayName("Transaktion werden nach Einfügen einer neuen Ausgabe neu kalkuliert")
  void test22() throws GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = new Gruppe("Simon's Bande", "Simon");
    gruppe.addMitglied("rafa");
    service.addGruppenAusgabe(gruppe, "Simon", Set.of("Simon", "rafa"), "wett",
        Money.of(1000, "EUR"));
    assertThat(gruppe.getTransaktionen()).isNotEmpty();
  }

  @Test
  @DisplayName("calculate gibt Ergebnis für Beispiel aus Aufgabenstellung")
  void test23() {
    Gruppe gruppe1 = new Gruppe("coole gruppe", "A");
    gruppe1.addMitglied("B");
    gruppe1.addMitglied("C");
    gruppe1.addAusgabe("A", Set.of("A", "B", "C"), "Hotel", Money.of(100, "EUR"));
    gruppe1.addAusgabe("A", Set.of("B"), "Hinfahrt B", Money.of(100, "EUR"));
    Set<Transaktion> beispiel = Set.of(
        new Transaktion("B", Money.of(133.33, "EUR"), "A"),
        new Transaktion("C", Money.of(33.33, "EUR"), "A")
    );
    assertThat(service.calculate(gruppe1)).isEqualTo(beispiel);
  }


  @Test
  @DisplayName("addMember funktioniert")
  void test24() {
    Gruppe gruppe1 = new Gruppe("coole gruppe", "A");
    service.addMitglied(gruppe1, "Klaus");
    assertThat(gruppe1.getMitglieder()).contains("Klaus");
  }

  @Test
  @DisplayName("addMember fügt keine Person doppelt hinzu")
  void test25() {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    service.addMitglied(gruppe, "Klaus");
    assertThat(gruppe.getMitglieder()).containsExactly("Klaus");
  }

}
