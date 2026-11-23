package cl.esfocar.api.documentos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documentos")
@CrossOrigin
public class DocumentoPdfController {
    private final DocumentoPdfService service;

    public DocumentoPdfController(DocumentoPdfService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoResumen subir(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestPart("archivo") MultipartFile archivo,
        @RequestParam(defaultValue = "") String resumen,
        @RequestParam(defaultValue = "General") String categoria
    ) {
        return DocumentoResumen.desde(service.guardar(authorization, archivo, resumen, categoria));
    }

    @GetMapping("/mios")
    public List<DocumentoResumen> mios(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.listarPropios(authorization).stream().map(DocumentoResumen::desde).toList();
    }

    @GetMapping("/compartidos")
    public List<DocumentoResumen> compartidos(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.listarCompartidos(authorization).stream().map(DocumentoResumen::desde).toList();
    }

    @PostMapping("/{documentoId}/compartir")
    @ResponseStatus(HttpStatus.CREATED)
    public ComparticionMaterial compartir(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @PathVariable String documentoId,
        @Valid @RequestBody CompartirRequest body
    ) {
        return service.compartir(authorization, documentoId, body.codigoAlumno());
    }

    @GetMapping("/{documentoId}/contenido")
    public ResponseEntity<Resource> descargar(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @PathVariable String documentoId
    ) {
        DocumentoPdfService.ArchivoPdfDescargable archivo = service.descargar(authorization, documentoId);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .cacheControl(org.springframework.http.CacheControl.noStore())
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(archivo.nombre(), StandardCharsets.UTF_8)
                .build()
                .toString())
            .body(archivo.recurso());
    }

    @DeleteMapping("/{documentoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @PathVariable String documentoId
    ) {
        service.eliminar(authorization, documentoId);
    }

    public record CompartirRequest(@NotBlank @Size(max = 80) String codigoAlumno) {}

    public record DocumentoResumen(
        String id,
        String propietario,
        String nombre,
        String resumen,
        String categoria,
        long bytes,
        java.time.Instant creadoEn
    ) {
        static DocumentoResumen desde(DocumentoPdf documento) {
            return new DocumentoResumen(
                documento.getId(),
                documento.getPropietario(),
                documento.getNombre(),
                documento.getResumen(),
                documento.getCategoria(),
                documento.getBytes(),
                documento.getCreadoEn()
            );
        }
    }
}
