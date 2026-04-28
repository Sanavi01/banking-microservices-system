# AGENTS.md — ASDD Project

> Canonical shared version: this file is the source of truth for shared agent guidelines.

This file defines general guidance for all AI agents working in this repository, following the **ASDD (Agent Spec Software Development)** workflow.

## Project Summary

> Ver `README.md` en la raíz del proyecto para stack, arquitectura y estructura de carpetas.
> Ver `.opencode/README.md` para la estructura completa del framework ASDD.

**Stack:** Java 17 + Spring Boot 3.x + JPA/Hibernate + PostgreSQL + RabbitMQ + Gradle  
**Microservicios:** `ms-clientes-personas` (8081) + `ms-cuentas-movimientos` (8082)  
**Paquete base:** `com.sofka.banking`

## ASDD Workflow

**Every new feature must follow this pipeline:**

```
[FASE 1 — Secuencial]
spec-generator    → /generate-spec      → .opencode/specs/<feature>.spec.md

[FASE 2 — Paralelo ∥]
database-agent    → modelos, migrations, seeders  (si hay cambios de DB)
backend-developer → capas del proyecto (controllers/services/repos/entities)

[FASE 3 — Secuencial]
test-engineer-backend  → tests/

[FASE 4 — Secuencial]
qa-agent          → /gherkin-case-generator, /risk-identifier, …

[FASE 5 — Opcional]
documentation-agent → README, API docs, ADRs
```

## Agent Skills (slash commands)

Skills are portable instruction sets invokable as `/command` in Copilot Chat.

### ASDD Core
| Skill | Slash Command | Descripción |
|-------|---------------|-------------|
| asdd-orchestrate | `/asdd-orchestrate` | Orquesta el flujo completo ASDD o consulta estado |
| generate-spec | `/generate-spec` | Genera spec técnica en `.opencode/specs/` |
| implement-backend | `/implement-backend` | Implementa feature completo en el backend |
| unit-testing | `/unit-testing` | Genera suite de tests (JUnit 5 + Mockito) |

### QA
| Skill | Slash Command | Descripción |
|-------|---------------|-------------|
| gherkin-case-generator | `/gherkin-case-generator` | Genera casos Given-When-Then + datos de prueba |
| risk-identifier | `/risk-identifier` | Clasifica riesgos con Regla ASD (Alto/Medio/Bajo) |
| automation-flow-proposer | `/automation-flow-proposer` | Propone flujos a automatizar y framework |
| performance-analyzer | `/performance-analyzer` | Planifica y analiza pruebas de performance |

## Lineamientos y Contexto

Los agentes deben cargar estos archivos como **primer paso** antes de generar cualquier código:

| Documento | Ruta | Agentes que lo cargan |
|---|---|---|
| Lineamientos de Desarrollo | `.opencode/docs/lineamientos/dev-guidelines.md` | Backend Developer, Database Agent |
| Lineamientos QA | `.opencode/docs/lineamientos/qa-guidelines.md` | Test Engineer Backend, QA Agent |
| Reglas de Oro | `.opencode/AGENTS.md` | Todos (siempre activas) |
| Definition of Done | `.opencode/copilot-instructions.md` | Test Engineer Backend, QA Agent, Orchestrator |
| Definition of Ready | `.opencode/copilot-instructions.md` | Spec Generator, Orchestrator |
| Stack y restricciones | `.opencode/instructions/backend.instructions.md` | Backend Developer, Database Agent, Spec Generator |
| Arquitectura | `.opencode/instructions/backend.instructions.md` | Backend Developer, Spec Generator |

---

## Reglas de Oro

> Principio rector: todas las contribuciones de la IA deben ser seguras, transparentes, con propósito definido y alineadas con las instrucciones explícitas del usuario.

### I. Integridad del Código y del Sistema
- **No código no autorizado**: no escribir, generar ni sugerir código nuevo a menos que el usuario lo solicite explícitamente.
- **No modificaciones no autorizadas**: no modificar, refactorizar ni eliminar código, archivos o estructuras existentes sin aprobación explícita del usuario.
- **Preservar la lógica existente**: respetar patrones arquitectónicos, estilo de codificación y lógica operativa del proyecto.

### II. Clarificación de Requisitos
- **Clarificación obligatoria**: si la solicitud es ambigua, incompleta o poco clara, detenerse y solicitar clarificación antes de proceder.
- **No realizar suposiciones**: basar todas las acciones estrictamente en información explícita proporcionada por el usuario.

### III. Transparencia Operativa
- **Explicar antes de actuar**: antes de cualquier acción, explicar qué se va a hacer y posibles implicaciones.
- **Detención ante la incertidumbre**: si surge inseguridad o un conflicto con estas reglas, detenerse y consultar al usuario.
- **Acciones orientadas a un propósito**: cada acción debe ser directamente relevante para la solicitud explícita.

---

## Entradas al Pipeline ASDD

| Tipo | Directorio | Descripción |
|------|-----------|-------------|
| Requerimientos de negocio | `.opencode/requirements/` | Input: descripción funcional del feature |
| Especificaciones técnicas | `.opencode/specs/` | Output del Spec Generator, fuente de verdad para implementación |

## Critical Rules for All Agents

1. **No implementation without a spec.** Always check `.opencode/specs/` first.
2. **Backend architecture is layered** — follow the pattern: `Controller → Service → Repository → JPA Entity`. Never bypass layers.
3. **Dependency injection via Spring** — use constructor injection with `@RequiredArgsConstructor` or `@Autowired`. Inject services into controllers, repositories into services.
4. **Use Spring stereotypes**: `@RestController`, `@Service`, `@Repository`, `@Entity`.
5. **Exception handling** via `@ControllerAdvice` with `@ExceptionHandler` — never return raw stack traces.
6. **Never commit secrets or credentials** — `.env`, `application.properties` with passwords, and API keys must be in `.gitignore`.

## Development Commands & Integration Notes

> Ver `README.md` en la raíz del proyecto para comandos de build, test y ejecución.
