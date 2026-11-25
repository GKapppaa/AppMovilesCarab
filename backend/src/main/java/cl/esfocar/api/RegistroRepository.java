package cl.esfocar.api;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroRepository extends JpaRepository<Registro, Long> {
    List<Registro> findAllByTipoOrderByIdDesc(String tipo);
    Optional<Registro> findByTipoAndClave(String tipo, String clave);
    void deleteByTipoAndClave(String tipo, String clave);
}
