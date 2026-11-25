package cl.esfocar.api.documentos;

import cl.esfocar.api.aula.AulaService;
import cl.esfocar.api.sesion.SesionActiva;
import cl.esfocar.api.sesion.SesionService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentoPdfService {
    private final DocumentoPdfRepository documentos;
    private final ComparticionMaterialRepository comparticiones;
    private final SesionService sesiones;
    private final AulaService aula;
    private final Path directorio;
    private final long maxBytes;

    public DocumentoPdfService(
        DocumentoPdfRepository documentos,
        ComparticionMaterialRepository comparticiones,
        SesionService sesiones,
        AulaService aula,
        @Value("${app.documentos.directorio:./data/documentos}") String directorio,
        @Value("${app.documentos.max-bytes:20971520}") long maxBytes
    ) {
        this.documentos = documentos;
        this.comparticiones = comparticiones;
        this.sesiones = sesiones;
        this.aula = aula;
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    public DocumentoPdf guardar(
        String authorization,
        MultipartFile archivo,
        String resumen,
        String categoria
    ) {
        SesionActiva sesion = sesiones.requerir(authorization);
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un PDF");
        }
        if (archivo.getSize() > maxBytes) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "El PDF supera el máximo permitido");
        }
        String nombre = nombreSeguro(archivo.getOriginalFilename());
        byte[] bytes;
        try {
            bytes = archivo.getBytes();
        } catch (IOException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el PDF", error);
        }
        if (!parecePdf(bytes)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El archivo no es un PDF válido");
        }

        String id = UUID.randomUUID().toString();
        String carpetaPerfil = sesion.codigoPerfil().replaceAll("[^A-Za-z0-9_-]", "_");
        String claveArchivo = carpetaPerfil + "/" + id + ".pdf";
        Path destino = directorio.resolve(claveArchivo).normalize();
        if (!destino.startsWith(directorio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de archivo inválido");
        }

        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, bytes);
            DocumentoPdf documento = new DocumentoPdf(
                id,
                sesion.codigoPerfil(),
                nombre,
                claveArchivo,
                recortar(resumen, 700),
                categoriaSegura(categoria),
                bytes.length,
                Instant.now()
            );
            return documentos.save(documento);
        } catch (IOException error) {
            borrarSilencioso(destino);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar el PDF", error);
        } catch (RuntimeException error) {
            borrarSilencioso(destino);
            throw error;
        }
    }

    public List<DocumentoPdf> listarPropios(String authorization) {
        SesionActiva sesion = sesiones.requerir(authorization);
        return documentos.findAllByPropietarioOrderByCreadoEnDesc(sesion.codigoPerfil());
    }

    public List<DocumentoPdf> listarCompartidos(String authorization) {
        SesionActiva sesion = sesiones.requerir(authorization);
        if (!"ESTUDIANTE".equals(sesion.rol())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo perfiles de estudiante consultan material recibido");
        }
        return comparticiones.findAllByAlumnoOrderByCompartidoEnDesc(sesion.codigoPerfil()).stream()
            .map(ComparticionMaterial::getDocumentoId)
            .map(id -> documentos.findById(id).orElse(null))
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    @Transactional
    public ComparticionMaterial compartir(String authorization, String documentoId, String codigoAlumno) {
        SesionActiva sesion = sesiones.requerir(authorization);
        if (!"PROFESOR".equals(sesion.rol())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el profesor puede compartir material");
        }
        DocumentoPdf documento = documentos.findByIdAndPropietario(documentoId, sesion.codigoPerfil())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el PDF del profesor"));
        String alumno = codigoAlumno.trim().toUpperCase();
        aula.requerirRelacion(sesion.codigoPerfil(), alumno);
        if (comparticiones.existsByDocumentoIdAndAlumno(documentoId, alumno)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El material ya fue compartido con el estudiante");
        }
        return comparticiones.save(new ComparticionMaterial(
            UUID.randomUUID().toString(),
            documento.getId(),
            sesion.codigoPerfil(),
            alumno,
            Instant.now()
        ));
    }

    public ArchivoPdfDescargable descargar(String authorization, String documentoId) {
        SesionActiva sesion = sesiones.requerir(authorization);
        DocumentoPdf documento = documentos.findById(documentoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el documento"));
        boolean propietario = documento.getPropietario().equals(sesion.codigoPerfil());
        boolean compartido = "ESTUDIANTE".equals(sesion.rol())
            && comparticiones.existsByDocumentoIdAndAlumno(documentoId, sesion.codigoPerfil());
        if (!propietario && !compartido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este documento no está disponible para tu perfil");
        }
        Path archivo = directorio.resolve(documento.getClaveArchivo()).normalize();
        if (!archivo.startsWith(directorio) || !Files.isRegularFile(archivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el archivo PDF");
        }
        return new ArchivoPdfDescargable(new FileSystemResource(archivo), documento.getNombre());
    }

    @Transactional
    public void eliminar(String authorization, String documentoId) {
        SesionActiva sesion = sesiones.requerir(authorization);
        DocumentoPdf documento = documentos.findByIdAndPropietario(documentoId, sesion.codigoPerfil())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el documento"));
        comparticiones.deleteAllByDocumentoId(documentoId);
        documentos.delete(documento);
        borrarSilencioso(directorio.resolve(documento.getClaveArchivo()));
    }

    private String nombreSeguro(String original) {
        String nombre = original == null ? "material.pdf" : Path.of(original).getFileName().toString();
        if (!nombre.toLowerCase().endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El archivo debe tener extensión PDF");
        }
        return recortar(nombre.replaceAll("[\\r\\n\\\\/]", "_"), 180);
    }

    private String categoriaSegura(String categoria) {
        List<String> validas = List.of("General", "Clases", "Normativa", "Tareas", "Escaneos");
        String valor = categoria == null ? "General" : categoria.trim();
        if (!validas.contains(valor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría de documento inválida");
        }
        return valor;
    }

    private String recortar(String valor, int max) {
        String limpio = valor == null ? "" : valor.trim();
        return limpio.length() <= max ? limpio : limpio.substring(0, max);
    }

    private boolean parecePdf(byte[] bytes) {
        return bytes.length >= 5
            && bytes[0] == '%'
            && bytes[1] == 'P'
            && bytes[2] == 'D'
            && bytes[3] == 'F'
            && bytes[4] == '-';
    }

    private void borrarSilencioso(Path archivo) {
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException ignored) {
            // El log del request conserva el error principal; no se filtra la ruta del archivo.
        }
    }

    public record ArchivoPdfDescargable(Resource recurso, String nombre) {}
}
