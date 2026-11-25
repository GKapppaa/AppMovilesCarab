package cl.esfocar.api.sesion;

import java.time.Instant;

public record SesionActiva(String codigoPerfil, String rol, Instant expiraEn) {}
