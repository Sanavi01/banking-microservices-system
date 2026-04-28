/*
 * Plantilla para tests de repositorios Spring Data JPA con @DataJpaTest.
 * Copia este archivo a src/test/java/com/sofka/banking/.../repository/FeatureRepositoryTest.java
 */

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FeatureRepositoryTest {

    @Autowired
    private FeatureRepository repository;

    @Test
    @DisplayName("save() persiste y retorna la entidad con ID generado")
    void save_shouldPersistAndReturnWithId() {
        Feature entity = new Feature();
        entity.setName("Test Feature");
        Feature saved = repository.save(entity);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Feature");
    }

    @Test
    @DisplayName("findByName() retorna la entidad cuando existe")
    void findByName_shouldReturnEntity_whenExists() {
        Feature entity = new Feature();
        entity.setName("Unique Name");
        repository.save(entity);

        Optional<Feature> result = repository.findByName("Unique Name");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Unique Name");
    }

    @Test
    @DisplayName("findById() retorna vacío cuando no existe")
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Feature> result = repository.findById(999L);
        assertThat(result).isEmpty();
    }
}
