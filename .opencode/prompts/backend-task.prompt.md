---
name: backend-task
description: Implementa una funcionalidad en el backend Java Spring Boot basada en una spec ASDD aprobada.
argument-hint: "<nombre-feature> (debe existir .opencode/specs/<nombre-feature>.spec.md)"
agent: Backend Developer
tools:
  - edit/createFile
  - edit/editFiles
  - read/readFile
  - search/listDirectory
  - search
  - execute/runInTerminal
---

Implementa el backend para el feature especificado, siguiendo la spec aprobada.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

## Pasos obligatorios:

1. **Lee la spec** en `.opencode/specs/${input:featureName:nombre-feature}.spec.md` — si no existe, detente e informa al usuario.
2. **Revisa el código existente** en `src/main/java/com/sofka/banking/` para entender patrones actuales.
3. **Implementa en orden**:
   - `src/main/java/.../model/entity/` — entidad JPA con `@Entity`, `@Table`
   - `src/main/java/.../repository/` — interface que extiende `JpaRepository`
   - `src/main/java/.../service/` — servicio con lógica de negocio (`@Service`, `@Transactional`)
   - `src/main/java/.../controller/` — controlador REST (`@RestController`, `@RequestMapping`)
   - `src/main/java/.../model/dto/` — DTOs de entrada/salida (`@Data`, `@Valid`)
4. **Registra el componente** en el contexto de Spring (via `@RestController` / `@Service` / `@Repository` — se auto-detectan con `@SpringBootApplication`).
5. **Verifica compilación** ejecutando: `./gradlew build -x test`

## Restricciones:
- Sigue el patrón de DI de Spring: inyección por constructor con `@RequiredArgsConstructor` o `@Autowired`.
- NUNCA usar `new Service()` o `new Repository()`.
- Usar DTOs para request/response (nunca exponer entidades JPA directamente).
- Seguir las convenciones de `.opencode/instructions/backend.instructions.md`.
