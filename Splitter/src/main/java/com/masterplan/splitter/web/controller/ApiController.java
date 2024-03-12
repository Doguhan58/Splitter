package com.masterplan.splitter.web.controller;

import com.masterplan.splitter.application.entities.GruppenEntity;
import com.masterplan.splitter.application.entities.GruppenMinEntity;
import com.masterplan.splitter.application.entities.TransaktionEntity;
import com.masterplan.splitter.application.service.ApiService;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.exceptions.GruppeGeschlossenException;
import com.masterplan.splitter.exceptions.PersonKeinMitgliedException;
import com.masterplan.splitter.web.forms.AddAusgabeApiForm;
import com.masterplan.splitter.web.forms.CreateGruppeApiForm;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

  ApiService service;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public ApiController(ApiService service) {
    this.service = service;
  }

  @GetMapping("api/user/{githubName}/gruppen")
  public List<GruppenMinEntity> getGruppenByGithubName(
      @PathVariable("githubName") String githubName) {
    return service.getGruppenByGitName(githubName);
  }

  @GetMapping("api/gruppen/{ID}")
  public ResponseEntity<GruppenEntity> getGruppenDetails(@PathVariable("ID") String id) {
    try {
      return new ResponseEntity<>(service.getGruppeById(UUID.fromString(id)), HttpStatus.OK);
    } catch (NoSuchElementException | IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("api/gruppen/{ID}/schliessen")
  public ResponseEntity<Gruppe> gruppeSchliessen(@PathVariable("ID") String id) {
    try {
      service.gruppeSchliessen(UUID.fromString(id));
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (NoSuchElementException | IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("api/gruppen/{ID}/ausgleich")
  public ResponseEntity<Set<TransaktionEntity>> getTransaktionen(@PathVariable("ID") String id) {
    try {
      return new ResponseEntity<>(service.getTransaktionenById(UUID.fromString(id)), HttpStatus.OK);
    } catch (NoSuchElementException | IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("api/gruppen")
  public ResponseEntity<String> createGruppe(
      @Valid @RequestBody CreateGruppeApiForm createGruppeApiForm, BindingResult result) {
    if (result.hasErrors()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(
        service.createGruppe(createGruppeApiForm.name(), createGruppeApiForm.personen()).toString(),
        HttpStatus.CREATED);
  }

  @PostMapping("api/gruppen/{ID}/auslagen")
  public ResponseEntity<String> addAusgabe(
      @Valid @RequestBody AddAusgabeApiForm addAusgabeApiForm,
      BindingResult result,
      @PathVariable("ID") String id) {
    if (result.hasErrors()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    try {
      service.addAusgabe(
          UUID.fromString(id),
          addAusgabeApiForm.grund(),
          addAusgabeApiForm.glaeubiger(),
          addAusgabeApiForm.cent(),
          addAusgabeApiForm.schuldner());
      return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (NoSuchElementException | IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (GruppeGeschlossenException e) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    } catch (PersonKeinMitgliedException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }
}
