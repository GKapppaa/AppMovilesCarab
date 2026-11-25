package cl.esfocar.api.aula;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelacionAulaRepository extends JpaRepository<RelacionAula, String> {
    boolean existsByProfesorAndAlumno(String profesor, String alumno);
    List<RelacionAula> findAllByProfesorOrderByCreadoEnDesc(String profesor);
}
