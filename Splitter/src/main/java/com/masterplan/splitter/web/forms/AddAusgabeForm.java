package com.masterplan.splitter.web.forms;

import java.util.UUID;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public record AddAusgabeForm(
    @NotNull(message = "UUID darf nicht leer sein") UUID id,
    @NotBlank(message = "Kreditor darf nicht leer sein") String kreditor,
    @NotNull(message = "Kosten darf nicht leer sein")
    @Positive(message = "Kosten muss positiv sein")
    @Max(value = 1000000000000L, message = "Kosten muss kleiner-gleich 1000000000000 sein")
    Double kosten,
    @NotBlank(message = "Kommentar darf nicht leer sein")
    @Size(max = 20, message = "Kommentar darf nicht länger als 20 Zeichen sein")
    String beschreibung
) {

}
