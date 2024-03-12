package com.masterplan.splitter.web.forms;


import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public record AddPersonForm(
    @Pattern(regexp = "^[a-zA-Z0-9](?!.*[-.]{2})(.*[a-zA-Z0-9])?$", message = "Kein valider "
        + "GithubName")
    @Size(max = 39, message = "GithubName Maximal 39 Zeichen")
    @Size(min = 3, message = "GithubName Mindestens 3 Zeichen")
    @NotBlank(message = "GithubName darf nicht leer sein") String githubName,
    @NotNull(message = "Id darf nicht leer sein") UUID id
) {

}
