---
name: generate-tests
description: Genera pruebas unitarias para backend (JUnit 5 + Mockito + Spring Boot Test) basadas en la spec ASDD y el código implementado.
argument-hint: "<nombre-feature> (debe existir .opencode/specs/<nombre-feature>.spec.md)"
agent: Test Engineer Backend
tools:
  - edit/createFile
  - edit/editFiles
  - read/readFile
  - search/listDirectory
  - search
  - execute/runInTerminal
---

Genera pruebas unitarias completas para el feature especificado.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

## Pasos obligatorios:

1. **Lee la spec** en `.opencode/specs/${input:featureName:nombre-feature}.spec.md` — sección "Plan de Pruebas Unitarias".
2. **Revisa el código implementado** en `src/main/java/com/sofka/banking/` para entender la estructura actual.
3. **Genera los tests** en:
   - `src/test/java/.../controller/<Feature>ControllerTest.java` — integración con `MockMvc` + `@WebMvcTest`
   - `src/test/java/.../service/<Feature>ServiceTest.java` — unitarios con `Mockito` + `@ExtendWith(MockitoExtension.class)`
   - `src/test/java/.../repository/<Feature>RepositoryTest.java` — capa datos con `@DataJpaTest` + H2 en memoria
4. **Verifica** que los tests corren: `./gradlew test`

## Cobertura obligatoria por test:
- ✅ Happy path (flujo exitoso)
- ❌ Error path (excepciones, errores de red, datos inválidos)
- 🔲 Edge cases (campos vacíos, duplicados, permisos)

## Restricciones:
- Cada test debe ser independiente (no compartir estado).
- Mockear SIEMPRE las dependencias externas (DB, RabbitMQ, API).
- Usar BDDMockito: `given()`, `then()`, `willReturn()`, `willThrow()`.
- Assertions con AssertJ: `assertThat(result).isNotNull()...`.
- Usar `@DisplayName` para descripciones legibles en español.
- No usar `Thread.sleep()` — cero tests "flaky".
