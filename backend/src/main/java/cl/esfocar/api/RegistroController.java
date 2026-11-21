package cl.esfocar.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class RegistroController {
    private static final Set<String> TIPOS = Set.of("perfiles", "secciones", "mensajes", "materiales", "tareas");
    private final RegistroRepository repository;

    public RegistroController(RegistroRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/health")
    public Health health() {
        return new Health("ok", "ESFOCAR academic API");
    }

    @GetMapping("/{tipo}")
    public List<Registro> listar(@PathVariable String tipo) {
        validarTipo(tipo);
        return repository.findAllByTipoOrderByIdDesc(tipo);
    }

    @GetMapping("/{tipo}/{clave}")
    public Registro obtener(@PathVariable String tipo, @PathVariable String clave) {
        validarTipo(tipo);
        return repository.findByTipoAndClave(tipo, clave)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
    }

    @PostMapping("/{tipo}")
    @ResponseStatus(HttpStatus.CREATED)
    public Registro crear(@PathVariable String tipo, @Valid @RequestBody RegistroRequest body) {
        validarTipo(tipo);
        if (repository.findByTipoAndClave(tipo, body.clave()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La clave ya existe");
        }
        return repository.save(new Registro(tipo, body.clave().trim(), body.contenido()));
    }

    @PutMapping("/{tipo}/{clave}")
    public Registro actualizar(@PathVariable String tipo, @PathVariable String clave, @Valid @RequestBody ActualizarRequest body) {
        validarTipo(tipo);
        Registro record = repository.findByTipoAndClave(tipo, clave)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
        record.setContenido(body.contenido());
        return repository.save(record);
    }

    @DeleteMapping("/{tipo}/{clave}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void eliminar(@PathVariable String tipo, @PathVariable String clave) {
        validarTipo(tipo);
        if (repository.findByTipoAndClave(tipo, clave).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado");
        }
        repository.deleteByTipoAndClave(tipo, clave);
    }

    private void validarTipo(String tipo) {
        if (!TIPOS.contains(tipo)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de recurso inválido");
    }

    public record Health(String status, String service) {}
    public record RegistroRequest(@NotBlank @Size(max = 120) String clave, @NotBlank String contenido) {}
    public record ActualizarRequest(@NotBlank String contenido) {}
}
