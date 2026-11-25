package cl.esfocar.api.documentos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
    name = "documentos_pdf",
    indexes = @Index(name = "idx_documento_propietario", columnList = "propietario")
)
public class DocumentoPdf {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 80)
    private String propietario;

    @Column(nullable = false, length = 180)
    private String nombre;

    @Column(nullable = false, length = 120)
    private String claveArchivo;

    @Column(nullable = false, length = 700)
    private String resumen;

    @Column(nullable = false, length = 40)
    private String categoria;

    @Column(nullable = false)
    private long bytes;

    @Column(nullable = false)
    private Instant creadoEn;

    protected DocumentoPdf() {}

    public DocumentoPdf(
        String id,
        String propietario,
        String nombre,
        String claveArchivo,
        String resumen,
        String categoria,
        long bytes,
        Instant creadoEn
    ) {
        this.id = id;
        this.propietario = propietario;
        this.nombre = nombre;
        this.claveArchivo = claveArchivo;
        this.resumen = resumen;
        this.categoria = categoria;
        this.bytes = bytes;
        this.creadoEn = creadoEn;
    }

    public String getId() { return id; }
    public String getPropietario() { return propietario; }
    public String getNombre() { return nombre; }
    public String getClaveArchivo() { return claveArchivo; }
    public String getResumen() { return resumen; }
    public String getCategoria() { return categoria; }
    public long getBytes() { return bytes; }
    public Instant getCreadoEn() { return creadoEn; }
}
