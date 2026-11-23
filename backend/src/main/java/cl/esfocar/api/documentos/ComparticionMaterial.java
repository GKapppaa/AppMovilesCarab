package cl.esfocar.api.documentos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "comparticiones_pdf",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_comparticion_documento_alumno",
        columnNames = {"documentoId", "alumno"}
    )
)
public class ComparticionMaterial {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String documentoId;

    @Column(nullable = false, length = 80)
    private String profesor;

    @Column(nullable = false, length = 80)
    private String alumno;

    @Column(nullable = false)
    private Instant compartidoEn;

    protected ComparticionMaterial() {}

    public ComparticionMaterial(String id, String documentoId, String profesor, String alumno, Instant compartidoEn) {
        this.id = id;
        this.documentoId = documentoId;
        this.profesor = profesor;
        this.alumno = alumno;
        this.compartidoEn = compartidoEn;
    }

    public String getId() { return id; }
    public String getDocumentoId() { return documentoId; }
    public String getProfesor() { return profesor; }
    public String getAlumno() { return alumno; }
    public Instant getCompartidoEn() { return compartidoEn; }
}
