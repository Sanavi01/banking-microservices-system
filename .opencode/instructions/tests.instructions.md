---
applyTo: "**/test/**/*.java"
---

> **Scope**: Las reglas aplican a proyectos con tests en Java usando JUnit 5 + Mockito + Spring Boot Test. Principios: independencia, aislamiento, AAA, cobertura ≥ 80%.

# Instrucciones para Archivos de Pruebas Unitarias

## Principios

- **Independencia**: cada test es 100% independiente — sin estado compartido entre tests.
- **Aislamiento**: mockear SIEMPRE dependencias externas (DB, RabbitMQ, APIs REST, sistema de archivos).
- **Claridad**: nombre del test debe describir la función bajo prueba y el escenario (qué pasa cuando X).
- **Cobertura**: cubrir happy path, error path y edge cases para cada unidad.

## Estructura de archivos

```
src/test/java/com/sofka/banking/<microservicio>/
  controller/<Feature>ControllerTest.java      ← integración con MockMvc
  service/<Feature>ServiceTest.java              ← unitarios con Mockito
  repository/<Feature>RepositoryTest.java        ← capa datos con @DataJpaTest
```

## Convenciones

- Nombre de método: `[método]_[escenario]_[resultado]` (ej: `create_shouldReturnSavedEntity_whenValidData`)
- Usar `@DisplayName` para descripción en español legible.
- `@ExtendWith(MockitoExtension.class)` para tests de servicio con mocks.
- `@WebMvcTest` para tests de controlador con `MockMvc`.
- `@DataJpaTest` para tests de repositorio con H2 en memoria.
- Usar BDDMockito: `given()`, `then()`, `willReturn()`, `willThrow()`.
- Assertions con AssertJ: `assertThat(result).isNotNull().hasFieldOrPropertyWithValue(...)`.

### Ejemplo AAA

```java
@Test
@DisplayName("create() con datos válidos retorna DTO con ID generado")
void create_shouldReturnDto_whenValidData() {
    // GIVEN — preparar datos y contexto
    FeatureCreateDTO input = new FeatureCreateDTO();
    input.setName("Test");

    Feature savedEntity = new Feature();
    savedEntity.setId(1L);
    savedEntity.setName("Test");

    given(repository.save(any(Feature.class))).willReturn(savedEntity);

    // WHEN — ejecutar la acción bajo prueba
    FeatureResponseDTO result = service.create(input);

    // THEN — verificar el resultado esperado
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("Test");
    then(repository).should().save(any(Feature.class));
}
```

## Cobertura Mínima por Capa

| Capa | Escenarios obligatorios |
|------|------------------------|
| **Controller** | 200/201 happy path, 400 datos inválidos, 404 not found, 422 error de negocio |
| **Service** | Lógica happy path, errores de negocio, casos edge |
| **Repository** | save/findById/delete con @DataJpaTest |

## Nunca hacer

- Tests que dependen del orden de ejecución.
- Conexiones reales a PostgreSQL, RabbitMQ o APIs externas.
- `System.out.println` permanentes en tests.
- Lógica condicional dentro de un test (if/else).
- Usar `Thread.sleep()` para sincronización temporal (ceros tests "flaky").
- Compartir estado mutable entre tests.

---

## DoR de Automatización
Antes de automatizar un flujo, verificar:
- [ ] Caso ejecutado exitosamente en manual sin bugs críticos
- [ ] Caso de prueba detallado con datos identificados
- [ ] Viabilidad técnica comprobada
- [ ] Ambiente estable disponible
- [ ] Aprobación del equipo

## DoD de Automatización
Un script finaliza cuando:
- [ ] Código revisado por pares (pull request review)
- [ ] Datos desacoplados del código
- [ ] Integrado al pipeline de CI
- [ ] Con documentación y trazabilidad hacia la HU

> Para quality gates, pirámide de testing, TDD, CDC y nomenclatura Gherkin, ver `.opencode/docs/lineamientos/dev-guidelines.md` §7 y `.opencode/docs/lineamientos/qa-guidelines.md`.
