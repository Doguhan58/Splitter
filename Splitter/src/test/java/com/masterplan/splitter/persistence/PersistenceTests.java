package com.masterplan.splitter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.masterplan.splitter.application.entities.GruppenEntity;
import com.masterplan.splitter.application.entities.GruppenMinEntity;
import com.masterplan.splitter.application.repositoryabstraction.RepositoryPort;
import com.masterplan.splitter.application.service.ApiService;
import com.masterplan.splitter.application.service.SplitterService;
import com.masterplan.splitter.domain.aggregates.group.Ausgabe;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.domain.service.GruppenService;
import com.masterplan.splitter.exceptions.GruppeGeschlossenException;
import com.masterplan.splitter.exceptions.PersonKeinMitgliedException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.naming.NoPermissionException;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

@DataJdbcTest
@ActiveProfiles("test")
@Sql(scripts = "/CreateTestTables.sql", config = @SqlConfig(encoding = "utf-8"))
public class PersistenceTests {

  GruppenService gruppenService;

  @Autowired
  SpringDataSplitterRepository springrepository;

  RepositoryPort repo;

  @BeforeEach
  void init() {
    repo = new RepositoryPortImpl(springrepository);
    gruppenService = new GruppenService();
  }

  @Test
  @DisplayName("Eine Gruppe kann hinzugefügt werden")
  void test1() {
    SplitterService service = new SplitterService(gruppenService, repo);
    Gruppe gruppe = service.createGruppe("coole gruppe", "Klaus");
    assertThat(service.getGruppenByGithubName("Klaus"))
        .contains(gruppe);
  }

  @Test
  @DisplayName("GetGruppenByGithubName gibt richtige Gruppe zurück")
  void test2() {
    SplitterService service = new SplitterService(gruppenService, repo);
    Gruppe gruppe = service.createGruppe("coole Gruppe", "Simon");
    assertThat(service.getGruppenByGithubName("Simon"))
        .contains(gruppe);
  }

  @Test
  @DisplayName("GetMitgliederByGruppenId gibt Set von Mitgliedern zurück")
  void test3() throws NoPermissionException {
    SplitterService service = new SplitterService(gruppenService, repo);
    Gruppe gruppe = service.createGruppe("coole Gruppe", "Simon");
    service.addPerson(gruppe.getId(), "Simon", "Marie");
    assertThat(service.getMitgliederByGruppenId(gruppe.getId(), "Simon"))
        .containsAll(Set.of("Simon", "Marie"));
  }

  @Test
  @DisplayName("Personen können zur Gruppe hinzugefügt werden")
  void test4() throws NoPermissionException {
    SplitterService service = new SplitterService(gruppenService, repo);
    Gruppe gruppe = service.createGruppe("coole Gruppe", "Doguhan");
    Gruppe gruppe1 = service.addPerson(gruppe.getId(), "Doguhan", "Simon");
    assertThat(service.getGruppenByGithubName("Doguhan")).contains(gruppe1);
  }

  @Test
  @DisplayName("GetGruppeByID gibt richtige Gruppe zurück")
  void test5() throws NoPermissionException {
    SplitterService service = new SplitterService(gruppenService, repo);
    Gruppe gruppe = service.createGruppe("coole Gruppe", "Simon");
    assertThat(service.getGruppeById(gruppe.getId(), "Simon"))
        .isEqualTo(gruppe);
  }

  @Test
  @DisplayName("gruppeSchliessen wird in DB gespeichert")
  void test6() throws NoPermissionException {
    SplitterService service = new SplitterService(gruppenService, repo);
    Gruppe gruppe = service.createGruppe("coole Gruppe", "Simon");
    Gruppe gruppe1 = service.gruppeSchliessen(gruppe.getId(), "Simon");
    assertThat(gruppe1.isGeschlossen())
        .isTrue();
  }

  @Test
  @DisplayName("Ausgabe kann hinzugefügt werden")
  void test7()
      throws NoPermissionException, GruppeGeschlossenException, PersonKeinMitgliedException {
    SplitterService service = new SplitterService(gruppenService, repo);
    Gruppe gruppe = service.createGruppe("coole Gruppe", "Simon");
    Gruppe gruppe1 = service.addPerson(gruppe.getId(), "Simon", "Doguhan");
    service.addAusgabe(gruppe1.getId(), "Simon", Set.of("Doguhan"), "Doener",
        Money.of(12.50, "EUR"));
    assertThat(service.getGruppeById(gruppe.getId(), "Simon").getAusgaben())
        .isEqualTo(
            List.of(new Ausgabe("Simon", Set.of("Doguhan"), "Doener", Money.of(12.50, "EUR"))));
  }

  @Test
  @DisplayName("Gruppe kann erzeugt werden, durch API service")
  void test8() {
    ApiService service = new ApiService(gruppenService, repo);
    UUID id = service.createGruppe("coole Gruppe", Set.of("Simon"));
    assertThat(service.getGruppeById(id))
        .isEqualTo(new GruppenEntity(id, "coole Gruppe", Set.of("Simon"),
            false, Set.of()));
  }

  @Test
  @DisplayName("Gruppe wird durch service erstellt und id wird gefunden durch API service")
  void test9() {
    ApiService service = new ApiService(gruppenService, repo);
    SplitterService service2 = new SplitterService(gruppenService, repo);
    Gruppe gruppe = service2.createGruppe("coole Gruppe", "Simon");
    assertThat(service.getGruppeById(gruppe.getId()))
        .isEqualTo(new GruppenEntity(gruppe));
  }

  @Test
  @DisplayName("Gruppe wird durch Github namen gefunden, durch API service")
  void test10() {
    ApiService service = new ApiService(gruppenService, repo);
    UUID id = service.createGruppe("coole Gruppe", Set.of("Simon"));
    assertThat(service.getGruppenByGitName("Simon"))
        .isEqualTo(List.of(new GruppenMinEntity(id, "coole Gruppe", Set.of("Simon"))));
  }

  @Test
  @DisplayName("Gruppe kann geschlossen werden, durch API service")
  void test11() {
    ApiService service = new ApiService(gruppenService, repo);
    UUID id = service.createGruppe("coole Gruppe", Set.of("Simon"));
    service.gruppeSchliessen(id);
    assertThat(service.getGruppeById(id).geschlossen())
        .isTrue();
  }

  @Test
  @DisplayName("Ausgabe kann hinzugefügt werden, durch API service")
  void test12() throws GruppeGeschlossenException, PersonKeinMitgliedException {
    ApiService service = new ApiService(gruppenService, repo);
    UUID id = service.createGruppe("coole Gruppe", Set.of("Simon", "Doguhan"));
    service.addAusgabe(id, "Doener", "Simon", 120L, Set.of("Doguhan"));
    assertThat(service.getGruppeById(id).ausgaben())
        .hasSize(1);
  }
}
