package com.masterplan.splitter.application.service;

import com.masterplan.splitter.application.entities.GruppenEntity;
import com.masterplan.splitter.application.entities.GruppenMinEntity;
import com.masterplan.splitter.application.entities.TransaktionEntity;
import com.masterplan.splitter.application.repositoryabstraction.RepositoryPort;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.domain.service.GruppenService;
import com.masterplan.splitter.exceptions.GruppeGeschlossenException;
import com.masterplan.splitter.exceptions.PersonKeinMitgliedException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

  private final GruppenService service;

  private final RepositoryPort repo;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public ApiService(GruppenService service, RepositoryPort repo) {
    this.service = service;
    this.repo = repo;
  }

  public List<GruppenMinEntity> getGruppenByGitName(String githubName) {
    return repo.findGruppenByGithubName(githubName).stream().map(GruppenMinEntity::new).toList();
  }

  public GruppenEntity getGruppeById(UUID id) {
    return new GruppenEntity(repo.findById(id));
  }

  public Gruppe gruppeSchliessen(UUID id) {
    Gruppe gruppe = repo.findById(id);
    service.gruppeSchliessen(gruppe);
    return repo.save(gruppe);
  }

  public Set<TransaktionEntity> getTransaktionenById(UUID id) {
    Gruppe gruppe = repo.findById(id);
    return service.getTransaktionen(gruppe).stream()
        .map(TransaktionEntity::new)
        .collect(Collectors.toSet());
  }

  public UUID createGruppe(String gruppenName, Set<String> personen) {
    List<String> list = new ArrayList<>(personen.stream().toList());
    Gruppe gruppe = service.createGruppe(gruppenName, list.get(0));
    list.remove(0);
    if (!list.isEmpty()) {
      list.forEach(gruppe::addMitglied);
    }
    return repo.save(gruppe).getId();
  }

  public Gruppe addAusgabe(
      UUID id, String grund, String glaeubiger, Long cent, Set<String> schuldner)
      throws GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = repo.findById(id);
    service.addGruppenAusgabe(
        gruppe, glaeubiger, schuldner, grund, Money.of(cent, "EUR").scaleByPowerOfTen(-2));
    return repo.save(gruppe);
  }
}
