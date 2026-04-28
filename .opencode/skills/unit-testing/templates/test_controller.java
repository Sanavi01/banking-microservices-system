/*
 * Plantilla para tests de controladores con MockMvc.
 * Copia a src/test/java/com/sofka/banking/.../controller/FeatureControllerTest.java
 */

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeatureController.class)
class FeatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeatureService service;

    @Test
    @DisplayName("POST /api/v1/features con datos válidos retorna 201")
    void create_shouldReturn201_whenValidData() throws Exception {
        FeatureResponseDTO response = new FeatureResponseDTO();
        response.setId(1L);
        response.setName("Test");

        given(service.create(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/features")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test\", \"description\": \"Desc\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    @DisplayName("GET /api/v1/features/{id} con ID inexistente retorna 404")
    void getById_shouldReturn404_whenNotFound() throws Exception {
        given(service.findById(999L))
            .willThrow(new ResourceNotFoundException("Feature no encontrada: 999"));

        mockMvc.perform(get("/api/v1/features/999"))
            .andExpect(status().isNotFound());
    }
}
