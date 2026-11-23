package cl.esfocar.api.sesion;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sesiones")
@CrossOrigin
public class SesionController {
    private final SesionService service;

    public SesionController(SesionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SesionResponse abrir(@Valid @RequestBody SesionRequest request) {
        return service.abrir(request);
    }

    @GetMapping("/actual")
    public SesionActiva actual(@RequestHeader("Authorization") String authorization) {
        return service.requerir(authorization);
    }

    @DeleteMapping("/actual")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cerrar(@RequestHeader(value = "Authorization", required = false) String authorization) {
        service.cerrar(authorization);
    }
}
