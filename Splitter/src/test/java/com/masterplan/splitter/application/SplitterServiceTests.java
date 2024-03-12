package com.masterplan.splitter.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.masterplan.splitter.application.repositoryabstraction.RepositoryPort;
import com.masterplan.splitter.application.service.SplitterService;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.domain.service.GruppenService;
import com.masterplan.splitter.exceptions.GruppeGeschlossenException;
import com.masterplan.splitter.exceptions.PersonKeinMitgliedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.naming.NoPermissionException;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SplitterServiceTests {

  SplitterService service;

  RepositoryPort repo;

  GruppenService gruppenService;

  @BeforeEach
  void getNewSplitterService() {
    repo = mock(RepositoryPort.class);
    gruppenService = mock(GruppenService.class);
    service = new SplitterService(gruppenService, repo);
  }

  @Test
  @DisplayName("getGruppenByGithubName ruft die richtige Methode  mit richtigen Parametern auf")
  void test1() {
    service.getGruppenByGithubName("Klaus");
    verify(repo).findGruppenByGithubName("Klaus");
  }

  @Test
  @DisplayName("getGruppenByGithubName gibt Objekte aus dem Repo zurück")
  void test2() {
    List<Gruppe> gruppen = List.of(new Gruppe("tolle Gruppe", "Klaus"));
    when(repo.findGruppenByGithubName("Klaus")).thenReturn(gruppen);
    assertThat(service.getGruppenByGithubName("Klaus")).isEqualTo(gruppen);
  }

  @Test
  @DisplayName("Wenn Person nicht in Gruppe werfen wir Exception")
  void test3() {
    Gruppe gruppe = new Gruppe("tolle Gruppe", "Hans");
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(false);
    NoPermissionException thrown = assertThrows(
        NoPermissionException.class,
        () -> service.getGruppeById(UUID.randomUUID(), "Klaus"),
        "did not throw"
    );
    assertTrue(thrown.getMessage().contentEquals("Person hat keinen Zugriff auf die Gruppe"));
  }

  @Test
  @DisplayName("createGruppe ruft Domainservice mit richtigen Parametern auf")
  void test4() {
    service.createGruppe("TestGruppe", "Klaus");
    assertThat(verify(gruppenService).createGruppe("TestGruppe", "Klaus")).isNull();
    //using "assertThat.isNull()" because of spotBugs warning for unused return value, but return
    // value is not necessary for test
  }

  @Test
  @DisplayName("Application service saved Gruppe im repo")
  void test5() {
    Gruppe gruppe = service.createGruppe("TestGruppe", "Klaus");
    verify(repo).save(gruppe);
  }

  @Test
  @DisplayName("getGruppeById ruft Repo auf für Gruppe per ID")
  void test6() throws NoPermissionException {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = new Gruppe(id, "TestGruppe", Set.of("Tester"), true, false,
        new ArrayList<>(), new HashSet<>());
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Tester")).thenReturn(true);
    service.getGruppeById(id, "Tester");
    verify(repo).findById(id);
  }

  @Test
  @DisplayName("getGruppeById ruft Domain Service auf")
  void test7() throws NoPermissionException {
    Gruppe gruppe = new Gruppe("TestGruppe", "Tester");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Tester")).thenReturn(true);
    service.getGruppeById(UUID.randomUUID(), "Tester");
    verify(gruppenService).hasMitglied(eq(gruppe), any(String.class));
  }

  @Test
  @DisplayName("addPerson speichert Gruppe wieder im repo")
  void test8() throws NoPermissionException {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any())).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Simon")).thenReturn(false);
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(true);
    service.addPerson(UUID.randomUUID(), "Klaus", "Simon");
    verify(repo).save(gruppe);
  }

  @Test
  @DisplayName("addPerson ruft Domain auf und fügt erfolgreich person hinzu")
  void test9() throws NoPermissionException {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Simon")).thenReturn(false);
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(true);
    service.addPerson(UUID.randomUUID(), "Klaus", "Simon");
    verify(gruppenService).addMitglied(gruppe, "Simon");
  }

  @Test
  @DisplayName("addPerson will Person Hinzufügen, ohne Rechte")
  void test12() {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(false);
    NoPermissionException thrown = assertThrows(
        NoPermissionException.class,
        () -> service.addPerson(UUID.randomUUID(), "Klaus", "Klaus"),
        "did not throw"
    );
    assertTrue(thrown.getMessage().contentEquals("Person hat keinen Zugriff auf die Gruppe"));
  }

  @Test
  @DisplayName("Aufruf von gruppeSchliessen, um eine Gruppe zu schließen")
  void test11() throws NoPermissionException {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(true);
    service.gruppeSchliessen(UUID.randomUUID(), "Klaus");
    verify(gruppenService).gruppeSchliessen(gruppe);
  }

  @Test
  @DisplayName("Aufruf von gruppeSchliessen, um eine Gruppe zu schließen")
  void test13() throws NoPermissionException {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(true);
    service.gruppeSchliessen(UUID.randomUUID(), "Klaus");
    verify(repo).save(gruppe);
  }

  @Test
  @DisplayName("Aufruf von gruppeSchliessen ohne Rechte der Person")
  void test14() {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(false);
    NoPermissionException thrown = assertThrows(
        NoPermissionException.class,
        () -> service.gruppeSchliessen(UUID.randomUUID(), "Klaus"),
        "did not throw"
    );
    assertTrue(thrown.getMessage().contentEquals("Person hat keinen Zugriff auf die Gruppe"));
  }

  @Test
  @DisplayName("Aufruf von addAusgabe ohne Rechte der Person")
  void test15() {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(false);
    NoPermissionException thrown = assertThrows(
        NoPermissionException.class,
        () -> service.addAusgabe(UUID.randomUUID(), "Klaus", Set.of("Simon"), "Kino",
            Money.of(15, "EUR")),
        "did not throw"
    );
    assertTrue(thrown.getMessage().contentEquals("Person hat keinen Zugriff auf die Gruppe"));
  }

  @Test
  @DisplayName("Aufruf von addAusgabe mit richtigen Parametern, mit Repo Aufruf")
  void test16()
      throws NoPermissionException, GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(true);
    service.addAusgabe(UUID.randomUUID(), "Klaus", Set.of("Simon"), "Kino", Money.of(15, "EUR"));
    verify(repo).save(gruppe);
  }

  @Test
  @DisplayName("Aufruf von addAusgabe mit richtigen Parametern, mit Domain Service aufruf")
  void test17()
      throws NoPermissionException, GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(true);
    service.addAusgabe(UUID.randomUUID(), "Klaus", Set.of("Simon"), "Kino", Money.of(15, "EUR"));
    verify(gruppenService).addGruppenAusgabe(gruppe, "Klaus", Set.of("Simon"), "Kino",
        Money.of(15, "EUR"));
  }

  @Test
  @DisplayName("Aufruf von getMitgliederByGruppenId mit richtigen Parametern")
  void test18() throws NoPermissionException {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(true);
    Set<String> test = service.getMitgliederByGruppenId(UUID.randomUUID(), "Klaus");
    assertThat(test).contains("Klaus");
  }

  @Test
  @DisplayName("Aufruf von getMitgliederByGruppenId ohne Rechte")
  void test19() {
    Gruppe gruppe = new Gruppe("TestGruppe", "Tester");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    when(gruppenService.hasMitglied(gruppe, "Klaus")).thenReturn(false);
    NoPermissionException thrown = assertThrows(
        NoPermissionException.class,
        () -> service.getMitgliederByGruppenId(UUID.randomUUID(), "Klaus"),
        "did not throw"
    );
    assertTrue(thrown.getMessage().contentEquals("Person hat keinen Zugriff auf die Gruppe"));
  }


}
