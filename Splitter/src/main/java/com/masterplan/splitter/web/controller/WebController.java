package com.masterplan.splitter.web.controller;

import com.masterplan.splitter.application.service.SplitterService;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.exceptions.GruppeGeschlossenException;
import com.masterplan.splitter.exceptions.PersonKeinMitgliedException;
import com.masterplan.splitter.web.forms.AddAusgabeForm;
import com.masterplan.splitter.web.forms.AddPersonForm;
import com.masterplan.splitter.web.forms.CreateGruppeForm;
import com.masterplan.splitter.web.forms.GruppeSchliessenForm;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import javax.naming.NoPermissionException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.javamoney.moneta.Money;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

@Controller
public class WebController {

  private final SplitterService service;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public WebController(SplitterService service) {
    this.service = service;
  }

  @GetMapping("/")
  public String getOverview(OAuth2AuthenticationToken token, Model model,
      HttpServletRequest request) {
    String githubName = (String) token.getPrincipal().getAttributes().get("login");
    List<Gruppe> gruppen = service.getGruppenByGithubName(githubName);
    model.addAttribute("gruppen", gruppen);
    model.addAttribute("githubName", githubName);
    Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
    if (inputFlashMap != null) {
      model.addAttribute("messageAddGruppe", (String) inputFlashMap.get("messageAddGruppe"));
      model.addAttribute("messageGruppeSchliessen",
          (String) inputFlashMap.get("messageGruppeSchliessen"));
      model.addAttribute("messageAddAusgabe", (String) inputFlashMap.get("messageAddAusgabe"));
      model.addAttribute("messageException", (String) inputFlashMap.get("messageException"));
    }
    return "overview";
  }

  @PostMapping("/createGruppe")
  public String createGruppe(@Valid CreateGruppeForm createGruppeForm, BindingResult result,
      OAuth2AuthenticationToken token, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      redirectAttributes.addFlashAttribute("messageAddGruppe",
          result.getAllErrors().get(0).getDefaultMessage());
      return "redirect:/";
    }
    String gitHandle = (String) token.getPrincipal().getAttributes().get("login");
    Gruppe gruppe = service.createGruppe(createGruppeForm.groupName(), gitHandle);
    UUID id = gruppe.getId();
    return "redirect:/details/" + id;
  }

  @GetMapping("/details/{id}")
  public String getDetails(@PathVariable("id") UUID id, Model model,
      OAuth2AuthenticationToken token, HttpServletRequest request) throws NoPermissionException {
    String gitHandle = (String) token.getPrincipal().getAttributes().get("login");
    Gruppe gruppe = service.getGruppeById(id, gitHandle);
    model.addAttribute("gruppe", gruppe);
    model.addAttribute("githubName", gitHandle);
    Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
    if (inputFlashMap != null) {
      model.addAttribute("messageAddPerson", (String) inputFlashMap.get("messageAddPerson"));
      model.addAttribute("messageAddAusgabe", (String) inputFlashMap.get("messageAddAusgabe"));
    }
    return "details";
  }

  @PostMapping("/addMitglied")
  public String addMitglied(@Valid AddPersonForm addPersonForm, BindingResult result,
      RedirectAttributes redirectAttributes, OAuth2AuthenticationToken token)
      throws NoPermissionException {
    if (result.hasErrors()) {
      redirectAttributes.addFlashAttribute("messageAddPerson",
          result.getAllErrors().get(0).getDefaultMessage());
      return "redirect:/details/" + addPersonForm.id();
    }
    String gitHandle = (String) token.getPrincipal().getAttributes().get("login");
    service.addPerson(addPersonForm.id(), gitHandle, addPersonForm.githubName());
    return "redirect:/details/" + addPersonForm.id();
  }

  @PostMapping("/GruppeSchliessen")
  public String gruppeSchliessen(@Valid GruppeSchliessenForm gruppeSchliessenForm,
      BindingResult result,
      RedirectAttributes redirectAttributes, OAuth2AuthenticationToken token)
      throws NoPermissionException {
    if (result.hasErrors()) {
      redirectAttributes.addFlashAttribute("messageGruppeSchliessen",
          result.getAllErrors().get(0).getDefaultMessage());
      return "redirect:/";
    }
    String gitHandle = (String) token.getPrincipal().getAttributes().get("login");
    service.gruppeSchliessen(gruppeSchliessenForm.id(), gitHandle);
    return "redirect:/details/" + gruppeSchliessenForm.id();
  }

  @PostMapping("/addAusgabe")
  public String addAusgabe(@Valid AddAusgabeForm addAusgabeForm, BindingResult result,
      HttpServletRequest servletRequest, RedirectAttributes redirectAttributes,
      OAuth2AuthenticationToken token)
      throws NoPermissionException, GruppeGeschlossenException, PersonKeinMitgliedException {
    if (result.hasErrors()) {
      if (result.hasFieldErrors("id")) {
        redirectAttributes.addFlashAttribute("messageAddAusgabe",
            result.getAllErrors().get(0).getDefaultMessage());
        return "redirect:/";
      }
      redirectAttributes.addFlashAttribute("messageAddAusgabe",
          result.getAllErrors().get(0).getDefaultMessage());
      return "redirect:/details/" + addAusgabeForm.id();
    }
    String gitHandle = (String) token.getPrincipal().getAttributes().get("login");
    Set<String> debitors = new HashSet<>();
    Set<String> members = service.getMitgliederByGruppenId(addAusgabeForm.id(), gitHandle);
    members.forEach(
        member -> {
          if (servletRequest.getParameter("check" + member) != null) {
            debitors.add(member);
          }
        });
    if (debitors.isEmpty()) {
      redirectAttributes.addFlashAttribute("messageAddAusgabe", "debitors should not be empty");
      return "redirect:/details/" + addAusgabeForm.id();
    }
    service.addAusgabe(addAusgabeForm.id(), addAusgabeForm.kreditor(), debitors,
        addAusgabeForm.beschreibung(), Money.of(addAusgabeForm.kosten(), "EUR"));
    return "redirect:/details/" + addAusgabeForm.id();
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public String handleException(IllegalArgumentException exception,
      RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("messageException", exception.getMessage());
    return "redirect:/";
  }

  @ExceptionHandler(NoSuchElementException.class)
  public String handleException(NoSuchElementException exception,
      RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("messageException", exception.getMessage());
    return "redirect:/";
  }

  @ExceptionHandler(GruppeGeschlossenException.class)
  public String handleException(GruppeGeschlossenException exception,
      RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("messageException", exception.getMessage());
    return "redirect:/";
  }

  @ExceptionHandler(PersonKeinMitgliedException.class)
  public String handleException(PersonKeinMitgliedException exception,
      RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("messageException", exception.getMessage());
    return "redirect:/";
  }

  @ExceptionHandler(NoPermissionException.class)
  public String handleException(NoPermissionException exception,
      RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("messageException", exception.getMessage());
    return "redirect:/";
  }

}
