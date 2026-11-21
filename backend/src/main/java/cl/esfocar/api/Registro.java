package cl.esfocar.api;

import jakarta.persistence.*;

@Entity
@Table(name = "registros_api", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "clave"}))
public class Registro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String tipo;

    @Column(nullable = false, length = 120)
    private String clave;

    @Lob
    @Column(nullable = false)
    private String contenido;

    protected Registro() {}

    public Registro(String tipo, String clave, String contenido) {
        this.tipo = tipo;
        this.clave = clave;
        this.contenido = contenido;
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getClave() { return clave; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}
