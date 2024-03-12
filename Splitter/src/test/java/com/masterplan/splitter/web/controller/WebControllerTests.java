package com.masterplan.splitter.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.masterplan.splitter.application.service.SplitterService;
import com.masterplan.splitter.config.security.SecurityConfig;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.web.helper.WithMockOauth2User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WebController.class)
@Import({SecurityConfig.class})
public class WebControllerTests {

  @MockBean
  SplitterService service;

  @Autowired
  MockMvc mvc;

  @Test
  @DisplayName("Startseite ist mit eingeloggtem User erreichbar")
  @WithMockOauth2User(login = "tester")
  void test1() throws Exception {
    mvc.perform(get("/")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("Startseite ist ohne login nicht erreichbar / redirect auf github")
  void test2() throws Exception {
    mvc.perform(get("/")).andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("Homepage leitet Anfrage mit richtigen Parametern an richtige Methode weiter")
  @WithMockOauth2User(login = "Klaus")
  void test3() throws Exception {
    mvc.perform(get("/")).andExpect(status().isOk());
    verify(service).getGruppenByGithubName("Klaus");
  }

  @Test
  @DisplayName("GitHandle wird ins Model geschrieben")
  @WithMockOauth2User(login = "Klaus")
  void test4() throws Exception {
    mvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("githubName", "Klaus"));
  }

  @Test
  @DisplayName("Homepage gibt die richtige view zurück")
  @WithMockOauth2User(login = "Klaus")
  void test5() throws Exception {
    mvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("overview"));
  }

  @Test
  @DisplayName("die richtigen Gruppen werden ins Model geschrieben")
  @WithMockOauth2User(login = "Klaus")
  void test6() throws Exception {
    List<Gruppe> gruppen = List.of(new Gruppe("gruppe", "Klaus"));
    when(service.getGruppenByGithubName("Klaus")).thenReturn(gruppen);
    mvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("gruppen", gruppen));
  }

  @Test
  @DisplayName("createGroup: redirected auf richtige UUID wenn OAuth und Name existieren")
  @WithMockOauth2User(login = "Klaus")
  void test7() throws Exception {
    Gruppe gruppe = new Gruppe("coole Gruppe", "Klaus");
    when(service.createGruppe("coole Gruppe", "Klaus")).thenReturn(
        gruppe);
    mvc.perform(post("/createGruppe")
            .param("groupName", "coole Gruppe")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/details/" + gruppe.getId()));
  }

  @Test
  @DisplayName("createGroup geht nicht ohne OAuth und leitet auf github um")
  void test8() throws Exception {
    mvc.perform(post("/createGruppe")
            .param("groupName", "coole Gruppe")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/github"));
  }

  @Test
  @DisplayName("createGroup geht nicht ohne Gruppennamen")
  @WithMockOauth2User(login = "Klaus")
  void test9() throws Exception {
    mvc.perform(post("/createGruppe")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/"))
        .andExpect(flash().attributeExists("messageAddGruppe"));
  }

  @Test
  @DisplayName("createGroup geht nicht mit leerem Gruppennamen")
  @WithMockOauth2User(login = "Klaus")
  void test10() throws Exception {
    mvc.perform(post("/createGruppe")
            .param("groupName", "")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/"))
        .andExpect(flash().attributeExists("messageAddGruppe"));
  }

  @Test
  @DisplayName("createGroup ruft Service mit richtigen parametern auf")
  @WithMockOauth2User(login = "Klaus")
  void test12() throws Exception {
    when(service.createGruppe("coole Gruppe", "Klaus")).thenReturn(
        new Gruppe("coole Gruppe", "Klaus"));
    mvc.perform(post("/createGruppe").param("groupName", "coole Gruppe").with(csrf()))
        .andExpect(status().is3xxRedirection());
    verify(service).createGruppe("coole Gruppe", "Klaus");
  }

  @Test
  @DisplayName("Detailseite ist mit eingeloggtem User erreichbar de in Gruppe enthalten ist")
  @WithMockOauth2User(login = "tester")
  void test13() throws Exception {
    mvc.perform(get("/")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("Detailseite ist ohne login nicht erreichbar / redirect auf github")
  void test14() throws Exception {
    mvc.perform(get("/details/" + UUID.randomUUID())).andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("Detailseite Controller ruft richtige service-Methode mit richtigen Parametern auf")
  @WithMockOauth2User(login = "tester")
  void test15() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = new Gruppe(id, "TestGruppe", Set.of("Tester"), true, false,
        new ArrayList<>(), new HashSet<>());
    when(service.getGruppeById(any(), any())).thenReturn(gruppe);
    mvc.perform(get("/details/" + id))
        .andExpect(status().isOk());
    verify(service).getGruppeById(id, "tester");
  }

  @Test
  @DisplayName("Detailseite Controller beschreibt das Model richtig")
  @WithMockOauth2User(login = "tester")
  void test16() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = new Gruppe(id, "TestGruppe", Set.of("Tester"), true, false,
        new ArrayList<>(), new HashSet<>());
    when(service.getGruppeById(any(), eq("tester"))).thenReturn(gruppe);
    mvc.perform(get("/details/" + id))
        .andExpect(status().isOk())
        .andExpect(model().attribute("gruppe", gruppe));
  }

  @Test
  @DisplayName("Detailseite Controller gibt richtige view zurück")
  @WithMockOauth2User(login = "tester")
  void test17() throws Exception {
    when(service.getGruppeById(any(), any())).thenReturn(
        new Gruppe("Gruppe", "Klaus"));
    mvc.perform(get("/details/" + UUID.randomUUID()))
        .andExpect(status().isOk())
        .andExpect(view().name("details"));
  }

  @Test
  @DisplayName("addPerson ruft methode mit richtigen parametern auf")
  @WithMockOauth2User(login = "tester")
  void test18() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(post("/addMitglied")
            .param("id", String.valueOf(id))
            .param("githubName", "Simon")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/" + id));
    verify(service).addPerson(id, "tester", "Simon");
  }

  @Test
  @DisplayName("addPerson geht nicht ohne OAuth und leitet auf github um")
  void test19() throws Exception {
    mvc.perform(post("/addMitglied")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/github"));
  }

  @Test
  @DisplayName("addPerson geht nicht ohne Namen")
  @WithMockOauth2User(login = "Klaus")
  void test20() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(post("/addMitglied")
            .param("id", String.valueOf(id))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/" + id))
        .andExpect(flash().attributeExists("messageAddPerson"));
  }

  @Test
  @DisplayName("addPerson geht nicht ohne id")
  @WithMockOauth2User(login = "Klaus")
  void test21() throws Exception {
    mvc.perform(post("/addMitglied")
            .param("githubName", "Simon")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/null"))
        .andExpect(flash().attributeExists("messageAddPerson"));
  }

  @Test
  @DisplayName("addPerson geht nicht mit invalidem Namen, kleiner als 3 zeichen")
  @WithMockOauth2User(login = "Klaus")
  void test22() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(post("/addMitglied")
            .param("id", String.valueOf(id))
            .param("githubName", "ab")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/" + id))
        .andExpect(flash().attributeExists("messageAddPerson"));
  }

  @Test
  @DisplayName("addPerson geht nicht mit invalidem Namen ,keine Sonderzeichen am Anfang")
  @WithMockOauth2User(login = "Klaus")
  void test23() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(post("/addMitglied")
            .param("id", String.valueOf(id))
            .param("githubName", "-cab")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/" + id))
        .andExpect(flash().attributeExists("messageAddPerson"));
  }

  @Test
  @DisplayName("addPerson geht nicht mit leerem Namen")
  @WithMockOauth2User(login = "Klaus")
  void test24() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(post("/addMitglied")
            .param("id", String.valueOf(id))
            .param("githubName", "")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/" + id))
        .andExpect(flash().attributeExists("messageAddPerson"));
  }

  @Test
  @DisplayName("finishGroup ruft methode auf")
  @WithMockOauth2User(login = "tester")
  void test25() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(post("/GruppeSchliessen")
            .param("id", String.valueOf(id))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/" + id));
    verify(service).gruppeSchliessen(id, "tester");
  }

  @Test
  @DisplayName("finishGroup funktioniert nicht ohne OAuth")
  void test26() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(post("/GruppeSchliessen")
            .param("id", String.valueOf(id))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/github"));
  }

  @Test
  @DisplayName("finishGroup geht nicht ohne id")
  @WithMockOauth2User(login = "Klaus")
  void test27() throws Exception {
    mvc.perform(post("/GruppeSchliessen")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/"));
  }

  @Test
  @DisplayName("addExpense ruft methode mit richtigen parametern auf")
  @WithMockOauth2User(login = "Tester")
  void test28() throws Exception {
    when(service.getMitgliederByGruppenId(any(), any())).thenReturn(
        Set.of("Tester", "Klaus", "Tester2"));
    UUID id = UUID.randomUUID();
    mvc.perform(post("/addAusgabe")
            .param("id", String.valueOf(id))
            .param("kreditor", "Tester")
            .param("kosten", "23.00")
            .param("beschreibung", "Kino")
            .param("checkTester", "on")
            .param("checkKlaus", "on")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/" + id));
    verify(service).addAusgabe(id, "Tester", Set.of("Tester", "Klaus"), "Kino",
        Money.of(23, "EUR"));
  }

  @Test
  @DisplayName("addExpense ruft methode ohne Kreditor auf")
  @WithMockOauth2User(login = "Tester")
  void test29() throws Exception {
    when(service.getMitgliederByGruppenId(any(), any())).thenReturn(
        Set.of("Tester", "Klaus", "Tester2"));
    UUID id = UUID.randomUUID();
    mvc.perform(post("/addAusgabe")
            .param("id", String.valueOf(id))
            .param("kosten", "23.00")
            .param("beschreibung", "Kino")
            .param("checkTester", "on")
            .param("checkKlaus", "on")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/" + id))
        .andExpect(flash().attributeExists("messageAddAusgabe"));
  }

  @Test
  @DisplayName("addExpense ruft methode ohne Debitoren auf")
  @WithMockOauth2User(login = "Tester")
  void test30() throws Exception {
    when(service.getMitgliederByGruppenId(any(), any())).thenReturn(
        Set.of("Tester", "Klaus", "Tester2"));
    UUID id = UUID.randomUUID();
    mvc.perform(post("/addAusgabe")
            .param("id", String.valueOf(id))
            .param("kreditor", "Tester")
            .param("kosten", "23.00")
            .param("beschreibung", "Kino")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/" + id))
        .andExpect(flash().attributeExists("messageAddAusgabe"));
  }

  @Test
  @DisplayName("addExpense ruft methode ohne id auf")
  @WithMockOauth2User(login = "Tester")
  void test31() throws Exception {
    mvc.perform(post("/addAusgabe")
            .param("kreditor", "Tester")
            .param("kosten", "23.00")
            .param("beschreibung", "Kino")
            .param("checkTester", "on")
            .param("checkKlaus", "on")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/"))
        .andExpect(flash().attributeExists("messageAddAusgabe"));
  }

  @Test
  @DisplayName("addExpense ruft methode ohne kosten auf")
  @WithMockOauth2User(login = "Tester")
  void test32() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(post("/addAusgabe")
            .param("id", String.valueOf(id))
            .param("kreditor", "Tester")
            .param("beschreibung", "Kino")
            .param("checkTester", "on")
            .param("checkKlaus", "on")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/" + id))
        .andExpect(flash().attributeExists("messageAddAusgabe"));
  }

  @Test
  @DisplayName("addExpense ruft methode ohne beschreibung auf")
  @WithMockOauth2User(login = "Tester")
  void test33() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(post("/addAusgabe")
            .param("id", String.valueOf(id))
            .param("kosten", "23.00")
            .param("kreditor", "Tester")
            .param("checkTester", "on")
            .param("checkKlaus", "on")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/details/" + id))
        .andExpect(flash().attributeExists("messageAddAusgabe"));
  }

  @Test
  @DisplayName("addExpense funktioniert nicht ohne OAuth")
  void test34() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(post("/addAusgabe")
            .param("id", String.valueOf(id))
            .param("kreditor", "Tester")
            .param("kosten", "23.00")
            .param("beschreibung", "Kino")
            .param("checkTester", "on")
            .param("checkKlaus", "on")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/github"));
  }

}
