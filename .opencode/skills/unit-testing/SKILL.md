---
name: unit-testing
description: Genera tests unitarios e integración (JUnit 5 + Mockito) para backend. Lee la spec y el código implementado. Requiere spec APPROVED e implementación completa.
argument-hint: "<nombre-feature>"
---

# Unit Testing

## Definition of Done — verificar al completar

- [ ] Cobertura ≥ 80% en lógica de negocio (quality gate bloqueante)
- [ ] Tests aislados — sin conexión a DB real (usa `@DataJpaTest` con H2 en memoria, mocks con Mockito)
- [ ] Escenario feliz + errores de negocio + validaciones de entrada cubiertos
- [ ] Los cambios no rompen contratos existentes del módulo

## Prerequisito — Lee en paralelo

```
.opencode/specs/<feature>.spec.md        (criterios de aceptación)
código implementado en src/main/java/
.opencode/instructions/backend.instructions.md   (JUnit 5 + Mockito + AssertJ)
```

## Output por scope

### Backend → `src/test/java/com/sofka/banking/`

| Archivo | Cubre |
|---------|-------|
| `controllers/<feature>ControllerTest.java` | Endpoints: 200/201, 400, 404, 422 con MockMvc |
| `services/<feature>ServiceTest.java` | Lógica: happy path + errores de negocio (Mockito) |
| `repositories/<feature>RepositoryTest.java` | Queries: `@DataJpaTest` con H2 en memoria |

## Patrones core

```java
// Backend — AAA con Mockito y AssertJ
@Test
@DisplayName("create() con datos válidos retorna DTO con ID")
void create_shouldReturnDto_whenValidData() {
    // Arrange
    FeatureCreateDTO dto = new FeatureCreateDTO();
    dto.setName("Test");
    Feature saved = new Feature();
    saved.setId(1L);
    saved.setName("Test");
    given(repository.save(any(Feature.class))).willReturn(saved);

    // Act
    FeatureResponseDTO result = service.create(dto);

    // Assert
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("Test");
    then(repository).should().save(any(Feature.class));
}
```

## Restricciones

- Solo `src/test/java/`. No modificar código fuente.
- Nunca conectar a DB real — usar `@DataJpaTest` con H2 o mocks con Mockito.
- Cobertura mínima ≥ 80% en lógica de negocio.
