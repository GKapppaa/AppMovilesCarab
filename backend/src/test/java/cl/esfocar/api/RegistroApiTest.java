package cl.esfocar.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:api-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.h2.console.enabled=false"
})
class RegistroApiTest {
    @Autowired MockMvc mvc;

    @Test
    void healthEndpointResponds() throws Exception {
        mvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void recordsSupportCreateReadUpdateAndDelete() throws Exception {
        mvc.perform(post("/api/v1/mensajes").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"clave":"msg-1","contenido":"{\\"alumnoId\\":\\"EST-000001\\",\\"texto\\":\\"Repasar unidad 1\\"}"}
                    """))
            .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/mensajes/msg-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clave").value("msg-1"));

        mvc.perform(put("/api/v1/mensajes/msg-1").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"contenido":"{\\"alumnoId\\":\\"EST-000001\\",\\"texto\\":\\"Repasar unidad 2\\"}"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.contenido").value(org.hamcrest.Matchers.containsString("unidad 2")));

        mvc.perform(delete("/api/v1/mensajes/msg-1")).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/mensajes/msg-1")).andExpect(status().isNotFound());
    }

    @Test
    void unknownResourceTypeIsRejected() throws Exception {
        mvc.perform(get("/api/v1/unknown")).andExpect(status().isBadRequest());
    }
}
