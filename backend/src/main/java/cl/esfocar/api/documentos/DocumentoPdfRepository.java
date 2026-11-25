package cl.esfocar.api.documentos;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoPdfRepository extends JpaRepository<DocumentoPdf, String> {
    List<DocumentoPdf> findAllByPropietarioOrderByCreadoEnDesc(String propietario);
    Optional<DocumentoPdf> findByIdAndPropietario(String id, String propietario);
}
