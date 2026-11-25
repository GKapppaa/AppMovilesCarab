package cl.esfocar.api.sesion;

import java.time.Instant;

public record SesionResponse(
    String token,
    String codigoPerfil,
    String rol,
    Instant expiraEn
) {}
