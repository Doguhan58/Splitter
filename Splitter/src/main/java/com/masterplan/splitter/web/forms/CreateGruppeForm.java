package com.masterplan.splitter.web.forms;

import javax.validation.constraints.NotBlank;

public record CreateGruppeForm(@NotBlank(message = "Name darf nicht leer sein") String groupName) {

}
