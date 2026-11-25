package cl.esfocar.api.sesion;

import jakarta.validation.constraints.NotBlank;

public record SesionRequest(
    @NotBlank String codigoPerfil,
    @NotBlank String rol
) {}
