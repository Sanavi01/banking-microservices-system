/*
 * Plantilla para tests de servicios con Mockito.
 * Copia a src/test/java/com/sofka/banking/.../service/FeatureServiceTest.java
 */

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FeatureServiceTest {

    @Mock
    private FeatureRepository repository;

    @InjectMocks
    private FeatureService service;

    @Test
    @DisplayName("create() con datos válidos retorna DTO con ID")
    void create_shouldReturnDto_whenValidData() {
        FeatureCreateDTO dto = new FeatureCreateDTO();
        dto.setName("Test");

        Feature saved = new Feature();
        saved.setId(1L);
        saved.setName("Test");

        given(repository.save(any(Feature.class))).willReturn(saved);

        FeatureResponseDTO result = service.create(dto);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test");
        then(repository).should().save(any(Feature.class));
    }

    @Test
    @DisplayName("update() lanza excepción cuando la entidad no existe")
    void update_shouldThrowException_whenEntityNotFound() {
        given(repository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999L, new FeatureCreateDTO()))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Feature no encontrada");
    }
}
