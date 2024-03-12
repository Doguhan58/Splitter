package com.masterplan.splitter.web.forms;

import java.util.UUID;
import javax.validation.constraints.NotNull;

public record GruppeSchliessenForm(
    @NotNull(message = "ID darf nicht null sein!") UUID id) {

}
