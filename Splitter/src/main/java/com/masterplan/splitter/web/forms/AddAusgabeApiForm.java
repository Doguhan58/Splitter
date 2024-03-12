package com.masterplan.splitter.web.forms;

import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public record AddAusgabeApiForm(
    @NotBlank @Size(max = 20) String grund,
    @NotBlank String glaeubiger,
    @NotNull @Positive Long cent,
    @NotEmpty Set<String> schuldner
) {

  public AddAusgabeApiForm(@NotBlank @Size(max = 20) String grund, @NotBlank String glaeubiger,
      @NotNull @Positive Long cent, @NotEmpty Set<String> schuldner) {
    this.grund = grund;
    this.glaeubiger = glaeubiger;
    this.cent = cent;
    this.schuldner = Set.copyOf(schuldner);
  }

  @Override
  public Set<String> schuldner() {
    return Set.copyOf(schuldner);
  }
}
