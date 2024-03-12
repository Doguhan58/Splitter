package com.masterplan.splitter.application.repositoryabstraction;

import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import java.util.List;
import java.util.UUID;

public interface RepositoryPort {

  List<Gruppe> findGruppenByGithubName(String githubName);

  List<Gruppe> findAll();

  Gruppe save(Gruppe gruppe);

  Gruppe findById(UUID id);

}
