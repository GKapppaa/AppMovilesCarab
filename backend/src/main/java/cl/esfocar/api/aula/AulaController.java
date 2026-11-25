package cl.esfocar.api.aula;

import cl.esfocar.api.sesion.SesionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/aula")
@CrossOrigin
public class AulaController {
    private final AulaService aula;
    private final SesionService sesiones;

    public AulaController(AulaService aula, SesionService sesiones) {
        this.aula = aula;
        this.sesiones = sesiones;
    }

    @PostMapping("/alumnos")
    @ResponseStatus(HttpStatus.CREATED)
    public AulaService.AlumnoVinculado vincular(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody VincularRequest body
    ) {
        return aula.vincular(sesiones.requerir(authorization), body.codigoAlumno());
    }

    @GetMapping("/alumnos")
    public List<AulaService.AlumnoVinculado> listar(
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return aula.listarAlumnos(sesiones.requerir(authorization));
    }

    public record VincularRequest(@NotBlank @Size(max = 80) String codigoAlumno) {}
}
