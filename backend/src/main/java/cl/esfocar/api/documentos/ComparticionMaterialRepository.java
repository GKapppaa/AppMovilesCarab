package cl.esfocar.api.documentos;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComparticionMaterialRepository extends JpaRepository<ComparticionMaterial, String> {
    boolean existsByDocumentoIdAndAlumno(String documentoId, String alumno);
    boolean existsByDocumentoIdAndProfesor(String documentoId, String profesor);
    List<ComparticionMaterial> findAllByAlumnoOrderByCompartidoEnDesc(String alumno);
    void deleteAllByDocumentoId(String documentoId);
    Optional<ComparticionMaterial> findByDocumentoIdAndAlumno(String documentoId, String alumno);
}
