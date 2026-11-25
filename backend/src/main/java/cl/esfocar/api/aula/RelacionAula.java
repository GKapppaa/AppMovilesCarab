package cl.esfocar.api.aula;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "relaciones_aula",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_relacion_profesor_alumno",
        columnNames = {"profesor", "alumno"}
    )
)
public class RelacionAula {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 80)
    private String profesor;

    @Column(nullable = false, length = 80)
    private String alumno;

    @Column(nullable = false)
    private Instant creadoEn;

    protected RelacionAula() {}

    public RelacionAula(String id, String profesor, String alumno, Instant creadoEn) {
        this.id = id;
        this.profesor = profesor;
        this.alumno = alumno;
        this.creadoEn = creadoEn;
    }

    public String getId() { return id; }
    public String getProfesor() { return profesor; }
    public String getAlumno() { return alumno; }
    public Instant getCreadoEn() { return creadoEn; }
}
