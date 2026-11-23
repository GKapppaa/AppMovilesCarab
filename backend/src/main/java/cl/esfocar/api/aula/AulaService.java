package cl.esfocar.api.aula;

import cl.esfocar.api.Registro;
import cl.esfocar.api.RegistroRepository;
import cl.esfocar.api.sesion.SesionActiva;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AulaService {
    private final RelacionAulaRepository relaciones;
    private final RegistroRepository registros;
    private final ObjectMapper mapper;

    public AulaService(RelacionAulaRepository relaciones, RegistroRepository registros, ObjectMapper mapper) {
        this.relaciones = relaciones;
        this.registros = registros;
        this.mapper = mapper;
    }

    @Transactional
    public AlumnoVinculado vincular(SesionActiva sesion, String codigoAlumno) {
        requerirRol(sesion, "PROFESOR");
        String alumno = codigoAlumno.trim().toUpperCase();
        if (sesion.codigoPerfil().equals(alumno)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes vincularte contigo mismo");
        }
        validarPerfil(alumno, "ESTUDIANTE");
        if (!relaciones.existsByProfesorAndAlumno(sesion.codigoPerfil(), alumno)) {
            relaciones.save(new RelacionAula(
                UUID.randomUUID().toString(),
                sesion.codigoPerfil(),
                alumno,
                Instant.now()
            ));
        }
        return perfilBasico(alumno, "ESTUDIANTE");
    }

    public List<AlumnoVinculado> listarAlumnos(SesionActiva sesion) {
        requerirRol(sesion, "PROFESOR");
        return relaciones.findAllByProfesorOrderByCreadoEnDesc(sesion.codigoPerfil())
            .stream()
            .map(RelacionAula::getAlumno)
            .map(codigo -> perfilBasico(codigo, "ESTUDIANTE"))
            .toList();
    }

    public void requerirRelacion(String profesor, String alumno) {
        if (!relaciones.existsByProfesorAndAlumno(profesor, alumno)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El estudiante no pertenece a tu sección");
        }
    }

    private AlumnoVinculado perfilBasico(String codigo, String rol) {
        Registro registro = validarPerfil(codigo, rol);
        try {
            JsonNode contenido = mapper.readTree(registro.getContenido());
            return new AlumnoVinculado(codigo, contenido.path("nombre").asText("Estudiante"));
        } catch (Exception error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El perfil remoto no tiene un formato válido");
        }
    }

    private Registro validarPerfil(String codigo, String rolEsperado) {
        Registro registro = registros.findByTipoAndClave("perfiles", codigo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el perfil indicado"));
        try {
            String rol = mapper.readTree(registro.getContenido()).path("rol").asText();
            if (!rolEsperado.equals(rol)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El perfil tiene un rol incorrecto");
            }
            return registro;
        } catch (ResponseStatusException error) {
            throw error;
        } catch (Exception error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El perfil remoto no tiene un formato válido");
        }
    }

    private void requerirRol(SesionActiva sesion, String rol) {
        if (!rol.equals(sesion.rol())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acción no disponible para este perfil");
        }
    }

    public record AlumnoVinculado(String codigoPerfil, String nombre) {}
}
