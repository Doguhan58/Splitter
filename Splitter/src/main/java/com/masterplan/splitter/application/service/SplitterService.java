package com.masterplan.splitter.application.service;

import com.masterplan.splitter.application.repositoryabstraction.RepositoryPort;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.domain.service.GruppenService;
import com.masterplan.splitter.exceptions.GruppeGeschlossenException;
import com.masterplan.splitter.exceptions.PersonKeinMitgliedException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.naming.NoPermissionException;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;

@Service
public class SplitterService {

  private final GruppenService service;

  private final RepositoryPort repo;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public SplitterService(GruppenService service, RepositoryPort repo) {
    this.service = service;
    this.repo = repo;
  }

  public List<Gruppe> getGruppenByGithubName(String githubName) {
    return repo.findGruppenByGithubName(githubName);
  }

  public Gruppe createGruppe(String gruppenName, String githubName) {
    Gruppe gruppe = service.createGruppe(gruppenName, githubName);
    return repo.save(gruppe);
  }

  public Gruppe getGruppeById(UUID id, String githubName) throws NoPermissionException {
    Gruppe gruppe = repo.findById(id);
    if (!service.hasMitglied(gruppe, githubName)) {
      throw new NoPermissionException("Person hat keinen Zugriff auf die Gruppe");
    }
    return gruppe;
  }

  public Gruppe addPerson(UUID id, String githubName, String addGithubName)
      throws NoPermissionException {
    Gruppe gruppe = this.getGruppeById(id, githubName);
    service.addMitglied(gruppe, addGithubName);
    return repo.save(gruppe);
  }

  public Gruppe gruppeSchliessen(UUID id, String githubName) throws NoPermissionException {
    Gruppe gruppe = this.getGruppeById(id, githubName);
    service.gruppeSchliessen(gruppe);
    return repo.save(gruppe);
  }

  public Gruppe addAusgabe(UUID id, String githubName, Set<String> debitoren, String kommentar,
      Money kosten)
      throws NoPermissionException, GruppeGeschlossenException, PersonKeinMitgliedException {
    Gruppe gruppe = this.getGruppeById(id, githubName);
    service.addGruppenAusgabe(gruppe, githubName, debitoren, kommentar, kosten);
    return repo.save(gruppe);
  }

  public Set<String> getMitgliederByGruppenId(UUID id, String githubName)
      throws NoPermissionException {
    Gruppe gruppe = this.getGruppeById(id, githubName);
    return gruppe.getMitglieder();
  }
}
