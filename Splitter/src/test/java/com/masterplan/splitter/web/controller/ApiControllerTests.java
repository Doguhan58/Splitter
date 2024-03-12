package com.masterplan.splitter.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.masterplan.splitter.application.entities.GruppenEntity;
import com.masterplan.splitter.application.entities.GruppenMinEntity;
import com.masterplan.splitter.application.entities.TransaktionEntity;
import com.masterplan.splitter.application.service.ApiService;
import com.masterplan.splitter.config.security.SecurityConfig;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.exceptions.GruppeGeschlossenException;
import com.masterplan.splitter.exceptions.PersonKeinMitgliedException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApiController.class)
@Import({SecurityConfig.class})
public class ApiControllerTests {

  @MockBean
  ApiService service;

  @Autowired
  MockMvc mvc;


  @Test
  @DisplayName("Anlegen der Gruppe mit 3 Personen und richtige id Return ")
  void test1() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.createGruppe("coole gruppe", Set.of("Simon", "Marie", "Rafa"))).thenReturn(id);
    String json = "{\"name\": \"coole gruppe\", \"personen\": [\"Simon\", \"Marie\", \"Rafa\"]}";

    mvc.perform(post("/api/gruppen").content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(content().string(id.toString()));
  }

  @Test
  @DisplayName("Anlegen der Gruppe mit 3 Personen fehlerhaft")
  void test2() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.createGruppe("", Set.of("Simon", "Marie", "Rafa"))).thenReturn(id);
    String json = "{\"name\": \"\", \"personen\": [\"Simon\", \"Marie\", \"Rafa\"]}";

    mvc.perform(post("/api/gruppen").content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Anlegen von Auslagen in einer existierenden Gruppe")
  void test3() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = new Gruppe("Coole Gruppe", "Simon");
    when(service.addAusgabe(id, "Doener", "Simon", 1200L, Set.of("Doguhan"))).thenReturn(gruppe);
    String json = "{\"grund\": \"Doener\", \"glaeubiger\": \"Simon\", \"cent\" : \"1200\", "
        + "\"schuldner\": "
        + "[\"Doguhan\"]}";

    mvc.perform(post("/api/gruppen/" + id + "/auslagen").content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("Anlegen von Auslagen einer non-existenten Gruppe")
  void test4() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = new Gruppe("Coole Gruppe", "Simon");
    when(service.addAusgabe(id, "Doener", "Simon", 1200L, Set.of("Doguhan"))).thenReturn(gruppe);
    String json = "{\"grund\": \"Doener\", \"glaeubiger\": \"Simon\", \"cent\" : \"1200\", "
        + "\"schuldner\": "
        + "[\"Doguhan\"]}";

    mvc.perform(post("/api/gruppen/" + 2 + "/auslagen").content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Anlegen von Auslagen in einer geschlossenen Gruppe")
  void test5() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.addAusgabe(id, "Doener", "Simon", 1200L, Set.of("Doguhan"))).thenThrow(
        GruppeGeschlossenException.class);
    String json = "{\"grund\": \"Doener\", \"glaeubiger\": \"Simon\", \"cent\" : \"1200\", "
        + "\"schuldner\": "
        + "[\"Doguhan\"]}";

    mvc.perform(post("/api/gruppen/" + id + "/auslagen").content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("Anlegen von Auslagen, wenn die Person kein Mitglied ist")
  void test6() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.addAusgabe(id, "Doener", "Ahmed", 1200L, Set.of("Doguhan"))).thenThrow(
        PersonKeinMitgliedException.class);
    String json = "{\"grund\": \"Doener\", \"glaeubiger\": \"Ahmed\", \"cent\" : \"1200\", "
        + "\"schuldner\": "
        + "[\"Doguhan\"]}";

    mvc.perform(post("/api/gruppen/" + id + "/auslagen").content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("schliessen einer Existierende Gruppe")
  void test7() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = new Gruppe("Coole Gruppe", "Simon");
    when(service.gruppeSchliessen(id)).thenReturn(gruppe);

    mvc.perform(post("/api/gruppen/" + id + "/schliessen"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("schliessen einer nicht-existierende Gruppe")
  void test8() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = new Gruppe("Coole Gruppe", "Simon");
    when(service.gruppeSchliessen(id)).thenReturn(gruppe);

    mvc.perform(post("/api/gruppen/" + 2 + "/schliessen"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Gibt alle Gruppen zurück vom User")
  void test9() throws Exception {
    UUID id = UUID.randomUUID();
    GruppenMinEntity resGrup = new GruppenMinEntity(id, "Coole Gruppe", Set.of("Simon"));
    when(service.getGruppenByGitName("Simon")).thenReturn(List.of(resGrup));

    mvc.perform(get("/api/user/Simon/gruppen"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].gruppe").value(id.toString()))
        .andExpect(jsonPath("$[0].name").value("Coole Gruppe"))
        .andExpect(jsonPath("$[0].personen").value("Simon"));
  }

  @Test
  @DisplayName("GetGruppenDetails funktioniert mit richtigen Parametern")
  void test10() throws Exception {
    UUID id = UUID.randomUUID();
    GruppenEntity resGrup = new GruppenEntity(id, "Coole Gruppe", Set.of("Simon"), false,
        Set.of());
    when(service.getGruppeById(id)).thenReturn(resGrup);

    mvc.perform(get("/api/gruppen/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gruppe").value(id.toString()))
        .andExpect(jsonPath("$.name").value("Coole Gruppe"))
        .andExpect(jsonPath("$.personen").value("Simon"))
        .andExpect(jsonPath("$.geschlossen").value(false))
        .andExpect(jsonPath("$.ausgaben").isEmpty());
  }

  @Test
  @DisplayName("GetGruppenDetails funktioniert nicht mit nicht existierende id")
  void test11() throws Exception {
    UUID id = UUID.randomUUID();
    GruppenEntity resGrup = new GruppenEntity(id, "Coole Gruppe", Set.of("Simon"), false,
        Set.of());
    when(service.getGruppeById(id)).thenReturn(resGrup);

    mvc.perform(get("/api/gruppen/" + 2))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GetGruppenDetails funktioniert mit richtigen Parametern")
  void test12() throws Exception {
    UUID id = UUID.randomUUID();
    TransaktionEntity transEnt = new TransaktionEntity("Simon", "Doguhan", 1200L);
    when(service.getTransaktionenById(id)).thenReturn(Set.of(transEnt));

    mvc.perform(get("/api/gruppen/" + id + "/ausgleich"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].von").value("Simon"))
        .andExpect(jsonPath("$[0].an").value("Doguhan"))
        .andExpect(jsonPath("$[0].cents").value(1200));
  }

  @Test
  @DisplayName("GetGruppenDetails funktioniert nicht bei falscher id")
  void test13() throws Exception {
    UUID id = UUID.randomUUID();
    TransaktionEntity transEnt = new TransaktionEntity("Simon", "Doguhan", 1200L);
    when(service.getTransaktionenById(id)).thenReturn(Set.of(transEnt));

    mvc.perform(get("/api/gruppen/" + 2 + "/ausgleich"))
        .andExpect(status().isNotFound());
  }
}
