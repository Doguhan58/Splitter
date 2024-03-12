package com.masterplan.splitter.web.forms;


import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

public record CreateGruppeApiForm(@NotBlank String name, @NotEmpty Set<String> personen) {

  public CreateGruppeApiForm(@NotBlank String name, @NotEmpty Set<String> personen) {
    this.name = name;
    this.personen = Set.copyOf(personen);
  }

  @Override
  public Set<String> personen() {
    return Set.copyOf(personen);
  }
}
