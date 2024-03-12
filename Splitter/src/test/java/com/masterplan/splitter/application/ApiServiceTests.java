package com.masterplan.splitter.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.masterplan.splitter.application.entities.GruppenEntity;
import com.masterplan.splitter.application.entities.GruppenMinEntity;
import com.masterplan.splitter.application.entities.TransaktionEntity;
import com.masterplan.splitter.application.repositoryabstraction.RepositoryPort;
import com.masterplan.splitter.application.service.ApiService;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.domain.aggregates.group.Transaktion;
import com.masterplan.splitter.domain.service.GruppenService;
import com.masterplan.splitter.exceptions.GruppeGeschlossenException;
import com.masterplan.splitter.exceptions.PersonKeinMitgliedException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ApiServiceTests {

  ApiService service;

  RepositoryPort repo;

  GruppenService gruppenService;

  @BeforeEach
  void getNewSplitterService() {
    repo = mock(RepositoryPort.class);
    gruppenService = mock(GruppenService.class);
    service = new ApiService(gruppenService, repo);
  }

  @Test
  @DisplayName("getGruppenByGithubName ruft die richtige Methode  mit richtigen Parametern auf")
  void test1() {
    service.getGruppenByGitName("Klaus");
    verify(repo).findGruppenByGithubName("Klaus");
  }

  @Test
  @DisplayName("getGruppenByGithubName gibt Objekte aus dem Repo zurück")
  void test2() {
    List<Gruppe> gruppes = List.of(new Gruppe("tolle Gruppe", "Klaus"));
    when(repo.findGruppenByGithubName("Klaus")).thenReturn(gruppes);
    List<GruppenMinEntity> right = List.of(
        new GruppenMinEntity(null, "tolle Gruppe", Set.of("Klaus")));
    assertThat(service.getGruppenByGitName("Klaus")).isEqualTo(right);
  }

  @Test
  @DisplayName("getGruppeById mit richtigen Parametern")
  void test3() {
    UUID uuid = UUID.randomUUID();
    Gruppe gruppe = new Gruppe("tolle Gruppe", "Klaus");
    when(repo.findById(uuid)).thenReturn(gruppe);
    GruppenEntity right = new GruppenEntity(null, "tolle Gruppe", Set.of("Klaus"), false, Set.of());
    assertThat(service.getGruppeById(uuid)).isEqualTo(right);
  }

  @Test
  @DisplayName("Aufruf von gruppeSchliessen, um eine Gruppe zu schließen, Domain")
  void test4() {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    service.gruppeSchliessen(UUID.randomUUID());
    verify(gruppenService).gruppeSchliessen(gruppe);
  }

  @Test
  @DisplayName("Aufruf von gruppeSchliessen, um eine Gruppe zu schließen,repo")
  void test5() {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.findById(any(UUID.class))).thenReturn(gruppe);
    service.gruppeSchliessen(UUID.randomUUID());
    verify(repo).save(gruppe);
  }

  @Test
  @DisplayName("Aufruf von getTransaktionenById mit richtigen Parameter")
  void test6() {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    gruppe.addMitglied("Kevin");
    Transaktion transaktion = new Transaktion("Klaus",
        Money.of(12.50, "EUR"), "Kevin");
    when(repo.findById(any())).thenReturn(gruppe);
    when(gruppenService.getTransaktionen(gruppe)).thenReturn(Set.of(transaktion));
    Set<TransaktionEntity> res = service.getTransaktionenById(gruppe.getId());
    Set<TransaktionEntity> right = Set.of(new TransaktionEntity("Klaus", "Kevin", 1250L));
    assertThat(res).isEqualTo(right);
  }

  @Test
  @DisplayName("createGruppe ruft Domainservice mit richtigen Parametern auf")
  void test7() {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(repo.save(any())).thenReturn(gruppe);
    service.createGruppe("TestGruppe", Set.of("Klaus"));
    assertThat(verify(gruppenService).createGruppe("TestGruppe", "Klaus")).isNull();
    //using "assertThat.isNull()" because of spotBugs warning for unused return value, but return
    // value is not necessary for test
  }

  @Test
  @DisplayName("ApplicationService saved Gruppe im Repo")
  void test8() {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(gruppenService.createGruppe(any(), any())).thenReturn(gruppe);
    when(repo.save(any())).thenReturn(gruppe);
    service.createGruppe("TestGruppe", Set.of("Klaus"));
    verify(repo).save(gruppe);
  }

  @Test
  @DisplayName("createGruppe gibt richite UUID zurück")
  void test9() {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    when(gruppenService.createGruppe(any(), any())).thenReturn(gruppe);
    when(repo.save(any())).thenReturn(gruppe);
    assertThat(service.createGruppe("TestGruppe", Set.of("Klaus"))).isEqualTo(gruppe.getId());
  }

  @Test
  @DisplayName("addAusgabe ruft GroupService mit richtigen Parametern auf")
  void test10() throws GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    gruppe.addMitglied("Peter");
    when(repo.findById(any())).thenReturn(gruppe);
    service.addAusgabe(gruppe.getId(), "Döner", "Klaus", 123L, Set.of("Peter"));
    verify(gruppenService).addGruppenAusgabe(gruppe, "Klaus", Set.of("Peter"), "Döner",
        Money.of(123,
            "EUR").scaleByPowerOfTen(-2));
  }

  @Test
  @DisplayName("addAusgabe wird mit richtigen Parametern aufgerufen im repo gesaved")
  void test11() throws GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = new Gruppe("TestGruppe", "Klaus");
    gruppe.addMitglied("Peter");
    when(repo.findById(any())).thenReturn(gruppe);
    service.addAusgabe(gruppe.getId(), "doener", "Klaus", 123L, Set.of("Peter"));
    verify(repo).save(gruppe);
  }


}
