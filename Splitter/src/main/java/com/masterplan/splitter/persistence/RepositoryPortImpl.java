package com.masterplan.splitter.persistence;

import com.masterplan.splitter.application.repositoryabstraction.RepositoryPort;
import com.masterplan.splitter.domain.aggregates.group.Ausgabe;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.domain.aggregates.group.Transaktion;
import com.masterplan.splitter.persistence.dto.AusgabeDto;
import com.masterplan.splitter.persistence.dto.DebitorenDto;
import com.masterplan.splitter.persistence.dto.GruppeDto;
import com.masterplan.splitter.persistence.dto.MitgliederDto;
import com.masterplan.splitter.persistence.dto.TransaktionDto;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Repository;

@Repository
public class RepositoryPortImpl implements RepositoryPort {

  SpringDataSplitterRepository repository;

  public RepositoryPortImpl(SpringDataSplitterRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Gruppe> findGruppenByGithubName(java.lang.String githubName) {
    return findAll().stream().filter(group -> group.hasMitglied(githubName)).toList();
  }

  @Override
  public List<Gruppe> findAll() {
    List<GruppeDto> all = repository.findAll();
    return all.stream().map(this::toGruppe).toList();
  }

  @Override
  public Gruppe save(Gruppe gruppe) {
    GruppeDto dto = fromGruppe(gruppe);
    GruppeDto saved = repository.save(dto);
    return toGruppe(saved);
  }

  @Override
  public Gruppe findById(UUID id) {
    return repository.findById(id).map(this::toGruppe).orElseThrow(
        () -> new NoSuchElementException("Es existiert keine Gruppe mit dieser ID"));
  }


  public Transaktion toTransaktion(TransaktionDto transaktionDto) {
    return new Transaktion(transaktionDto.name(), Money.of(transaktionDto.sendet(), "EUR"),
        transaktionDto.an());
  }

  public Ausgabe toAusgabe(AusgabeDto ausgabeDto) {
    return new Ausgabe(ausgabeDto.kreditor(),
        ausgabeDto.debitoren().stream().map(DebitorenDto::debitor).collect(Collectors.toSet()),
        ausgabeDto.beschreibung(), Money.of(ausgabeDto.kosten(), "EUR"));
  }

  public Gruppe toGruppe(GruppeDto gruppeDto) {
    return new Gruppe(
        gruppeDto.id(), gruppeDto.name(),
        gruppeDto.mitglieder().stream().map(MitgliederDto::mitglied).collect(Collectors.toSet()),
        gruppeDto.offenFuerPersonen(), gruppeDto.geschlossen(),
        gruppeDto.ausgaben().stream().map(this::toAusgabe).toList(),
        gruppeDto.transaktionen().stream().map(this::toTransaktion).collect(Collectors.toSet())
    );
  }

  public GruppeDto fromGruppe(Gruppe gruppe) {
    return new GruppeDto(
        gruppe.getId(),
        gruppe.getName(),
        gruppe.getMitglieder().stream().map(MitgliederDto::new).collect(Collectors.toSet()),
        gruppe.isOffenFuerPersonen(),
        gruppe.isGeschlossen(),
        gruppe.getAusgaben().stream().map(this::fromAusgabe).toList(),
        gruppe.getTransaktionen().stream().map(this::fromTransaktion).collect(Collectors.toSet()));
  }

  public TransaktionDto fromTransaktion(Transaktion transaktion) {
    return new TransaktionDto(transaktion.name(),
        transaktion.sendet().getNumber().doubleValue(), transaktion.an());
  }


  public AusgabeDto fromAusgabe(Ausgabe ausgabe) {
    return new AusgabeDto(0,
        ausgabe.kreditor(),
        ausgabe.debitoren().stream().map(DebitorenDto::new).collect(Collectors.toSet()),
        ausgabe.beschreibung(),
        ausgabe.kosten().getNumber().doubleValue());
  }


}
