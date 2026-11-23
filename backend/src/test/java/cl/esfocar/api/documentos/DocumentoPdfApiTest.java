package cl.esfocar.api.documentos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:pdf-api-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.h2.console.enabled=false",
    "app.documentos.directorio=build/test-documentos",
    "app.documentos.max-bytes=20971520"
})
class DocumentoPdfApiTest {
    @Autowired MockMvc mvc;

    private String codigoProfesor;
    private String codigoEstudiante;
    private String codigoEstudianteAjeno;
    private String tokenProfesor;
    private String tokenEstudiante;
    private String tokenEstudianteAjeno;

    @BeforeEach
    void prepararPerfilesYSesiones() throws Exception {
        codigoProfesor = "PROF-" + java.util.UUID.randomUUID().toString().toUpperCase();
        codigoEstudiante = "EST-" + java.util.UUID.randomUUID().toString().toUpperCase();
        codigoEstudianteAjeno = "EST-" + java.util.UUID.randomUUID().toString().toUpperCase();
        registrarPerfil(codigoProfesor, "Profesora Demo", "PROFESOR");
        registrarPerfil(codigoEstudiante, "Estudiante Demo", "ESTUDIANTE");
        registrarPerfil(codigoEstudianteAjeno, "Estudiante Ajeno", "ESTUDIANTE");
        tokenProfesor = abrirSesion(codigoProfesor, "PROFESOR");
        tokenEstudiante = abrirSesion(codigoEstudiante, "ESTUDIANTE");
        tokenEstudianteAjeno = abrirSesion(codigoEstudianteAjeno, "ESTUDIANTE");
    }

    @Test
    void guardaPorPropietarioYCompartePdfConAlumnoVinculado() throws Exception {
        mvc.perform(post("/api/v1/aula/alumnos")
                .header("Authorization", bearer(tokenProfesor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"codigoAlumno":"%s"}
                    """.formatted(codigoEstudiante)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigoPerfil").value(codigoEstudiante));

        MockMultipartFile pdf = new MockMultipartFile(
            "archivo",
            "unidad 1.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "%PDF-1.4\nContenido de prueba".getBytes()
        );
        String documentoId = mvc.perform(multipart("/api/v1/documentos")
                .file(pdf)
                .param("resumen", "Resumen de la unidad")
                .param("categoria", "Clases")
                .header("Authorization", bearer(tokenProfesor)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("unidad 1.pdf"))
            .andReturn()
            .getResponse()
            .getContentAsString()
            .replaceAll("(?s).*\"id\":\"([^\"]+)\".*", "$1");

        mvc.perform(post("/api/v1/documentos/{id}/compartir", documentoId)
                .header("Authorization", bearer(tokenProfesor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"codigoAlumno":"%s"}
                    """.formatted(codigoEstudiante)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.alumno").value(codigoEstudiante));

        mvc.perform(get("/api/v1/documentos/compartidos")
                .header("Authorization", bearer(tokenEstudiante)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(documentoId))
            .andExpect(jsonPath("$[0].propietario").value(codigoProfesor));

        mvc.perform(get("/api/v1/documentos/{id}/contenido", documentoId)
                .header("Authorization", bearer(tokenEstudiante)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(content().bytes("%PDF-1.4\nContenido de prueba".getBytes()));

        mvc.perform(get("/api/v1/documentos/{id}/contenido", documentoId)
                .header("Authorization", bearer(tokenEstudianteAjeno)))
            .andExpect(status().isForbidden());

        mvc.perform(delete("/api/v1/documentos/{id}", documentoId)
                .header("Authorization", bearer(tokenProfesor)))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/documentos/{id}/contenido", documentoId)
                .header("Authorization", bearer(tokenEstudiante)))
            .andExpect(status().isNotFound());
    }

    @Test
    void rechazaSubidaSinSesionYArchivoQueNoEsPdf() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
            "archivo", "nota.pdf", MediaType.APPLICATION_PDF_VALUE, "%PDF-1.4\n".getBytes()
        );
        mvc.perform(multipart("/api/v1/documentos").file(pdf))
            .andExpect(status().isUnauthorized());

        MockMultipartFile noPdf = new MockMultipartFile(
            "archivo", "nota.pdf", MediaType.APPLICATION_PDF_VALUE, "no es pdf".getBytes()
        );
        mvc.perform(multipart("/api/v1/documentos")
                .file(noPdf)
                .header("Authorization", bearer(tokenProfesor)))
            .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void estudianteNoPuedeVincularNiCompartirComoProfesor() throws Exception {
        mvc.perform(post("/api/v1/aula/alumnos")
                .header("Authorization", bearer(tokenEstudiante))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"codigoAlumno":"%s"}
                    """.formatted(codigoProfesor)))
            .andExpect(status().isForbidden());
    }

    @Test
    void profesorNoPuedeCompartirConAlumnoNoVinculado() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
            "archivo", "unidad.pdf", MediaType.APPLICATION_PDF_VALUE, "%PDF-1.4\nunidad".getBytes()
        );
        String response = mvc.perform(multipart("/api/v1/documentos")
                .file(pdf)
                .header("Authorization", bearer(tokenProfesor)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        String id = response.replaceAll("(?s).*\"id\":\"([^\"]+)\".*", "$1");

        mvc.perform(post("/api/v1/documentos/{id}/compartir", id)
                .header("Authorization", bearer(tokenProfesor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"codigoAlumno":"%s"}
                    """.formatted(codigoEstudianteAjeno)))
            .andExpect(status().isForbidden());

        mvc.perform(delete("/api/v1/documentos/{id}", id)
                .header("Authorization", bearer(tokenProfesor)))
            .andExpect(status().isNoContent());
    }

    private void registrarPerfil(String codigo, String nombre, String rol) throws Exception {
        mvc.perform(post("/api/v1/perfiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"clave":"%s","contenido":"{\\"nombre\\":\\"%s\\",\\"rol\\":\\"%s\\"}"}
                    """.formatted(codigo, nombre, rol)))
            .andExpect(status().isCreated());
    }

    private String abrirSesion(String codigo, String rol) throws Exception {
        String response = mvc.perform(post("/api/v1/sesiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"codigoPerfil":"%s","rol":"%s"}
                    """.formatted(codigo, rol)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return response.replaceAll("(?s).*\"token\":\"([^\"]+)\".*", "$1");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
