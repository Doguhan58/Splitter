package com.masterplan.splitter.persistence;

import com.masterplan.splitter.persistence.dto.GruppeDto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface SpringDataSplitterRepository extends CrudRepository<GruppeDto, UUID> {

  List<GruppeDto> findAll();

  Optional<GruppeDto> findById(UUID id);

}
