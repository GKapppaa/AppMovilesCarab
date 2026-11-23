package cl.esfocar.api.sesion;

import cl.esfocar.api.Registro;
import cl.esfocar.api.RegistroRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Sesiones efímeras para el MVP académico. La app ya validó el acceso en su cuenta
 * local; esto emite un bearer para limitar los recursos remotos a un perfil/rol.
 * No reemplaza autenticación de producción.
 */
@Service
public class SesionService {
    private static final Duration DURACION = Duration.ofHours(12);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, SesionActiva> sesiones = new ConcurrentHashMap<>();
    private final RegistroRepository registros;
    private final ObjectMapper mapper;

    public SesionService(RegistroRepository registros, ObjectMapper mapper) {
        this.registros = registros;
        this.mapper = mapper;
    }

    public SesionResponse abrir(SesionRequest request) {
        String codigo = request.codigoPerfil().trim().toUpperCase();
        String rol = normalizarRol(request.rol());
        Registro perfil = registros.findByTipoAndClave("perfiles", codigo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Perfil no sincronizado"));
        validarRolPerfil(perfil, rol);

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiracion = Instant.now().plus(DURACION);
        sesiones.put(token, new SesionActiva(codigo, rol, expiracion));
        return new SesionResponse(token, codigo, rol, expiracion);
    }

    public SesionActiva requerir(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Se requiere una sesión");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        SesionActiva sesion = sesiones.get(token);
        if (sesion == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida");
        if (sesion.expiraEn().isBefore(Instant.now())) {
            sesiones.remove(token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión expirada; inicia sesión nuevamente");
        }
        return sesion;
    }

    public void cerrar(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) return;
        sesiones.remove(authorization.substring("Bearer ".length()).trim());
    }

    private String normalizarRol(String rol) {
        String normalizado = rol.trim().toUpperCase();
        if (!normalizado.equals("ESTUDIANTE") && !normalizado.equals("PROFESOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido");
        }
        return normalizado;
    }

    private void validarRolPerfil(Registro perfil, String rol) {
        try {
            JsonNode contenido = mapper.readTree(perfil.getContenido());
            if (!rol.equals(contenido.path("rol").asText())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El rol no corresponde al perfil");
            }
        } catch (ResponseStatusException error) {
            throw error;
        } catch (Exception error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El perfil remoto no tiene un formato válido");
        }
    }
}
