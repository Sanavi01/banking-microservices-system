# Copilot Instructions

## ASDD Workflow (Agent Spec Software Development)

Este repositorio sigue el flujo **ASDD**: toda funcionalidad nueva se ejecuta en fases orquestadas por agentes especializados.

```
[Orchestrator] → [Spec Generator] → [Backend ∥ DB] → [Tests] → [QA] → [Doc]
```

### Fases del flujo ASDD
1. **Spec**: El agente `spec-generator` genera la spec en `.opencode/specs/<feature>.spec.md`.
2. **Implementación (paralelo)**: `backend-developer` + `database-agent` (si hay cambios de DB).
3. **Tests**: `test-engineer-backend` genera pruebas unitarias y de integración.
4. **QA**: `qa-agent` genera estrategia, Gherkin, riesgos y análisis de performance.
5. **Doc (opcional)**: `documentation-agent` genera README updates, API docs y ADRs.

### Skills disponibles (slash commands):
- `/asdd-orchestrate` — orquesta el flujo completo ASDD o consulta estado
- `/generate-spec` — genera spec técnica en `.opencode/specs/`
- `/implement-backend` — implementa feature completo en el backend
- `/unit-testing` — genera suite de tests
- `/gherkin-case-generator` — casos Given-When-Then + datos de prueba
- `/risk-identifier` — clasificación de riesgos ASD (Alto/Medio/Bajo)
- `/automation-flow-proposer` — propuesta de automatización con ROI
- `/performance-analyzer` — planificación de pruebas de performance

### Requerimientos y Specs
- Los requerimientos de negocio viven en `.opencode/requirements/`. Son la entrada al pipeline ASDD.
- Las specs técnicas viven en `.opencode/specs/`. Cada spec es la fuente de verdad para implementar.
- Antes de implementar cualquier desarrollo, debe existir una spec aprobada en `.opencode/specs/`.
- Flujo: `requirements/<feature>.md` → `/generate-spec` → `specs/<feature>.spec.md` (APPROVED)

---

## Mapa de Archivos ASDD

### Agentes
| Agente | Fase | Ruta |
|---|---|---|
| Orchestrator | Entry point | `.opencode/agents/orchestrator.agent.md` |
| Spec Generator | Fase 1 | `.opencode/agents/spec-generator.agent.md` |
| Backend Developer | Fase 2 | `.opencode/agents/backend-developer.agent.md` |
| Database Agent | Fase 2 | `.opencode/agents/database.agent.md` |
| Test Engineer Backend | Fase 3 | `.opencode/agents/test-engineer-backend.agent.md` |
| QA Agent | Fase 4 | `.opencode/agents/qa.agent.md` |
| Documentation Agent | Fase 5 | `.opencode/agents/documentation.agent.md` |

### Skills
| Skill | Agente | Ruta |
|---|---|---|
| `/asdd-orchestrate` | Orchestrator | `.opencode/skills/asdd-orchestrate/SKILL.md` |
| `/generate-spec` | Spec Generator | `.opencode/skills/generate-spec/SKILL.md` |
| `/implement-backend` | Backend Developer | `.opencode/skills/implement-backend/SKILL.md` |
| `/unit-testing` | Test Engineer Backend | `.opencode/skills/unit-testing/SKILL.md` |
| `/gherkin-case-generator` | QA Agent | `.opencode/skills/gherkin-case-generator/SKILL.md` |
| `/risk-identifier` | QA Agent | `.opencode/skills/risk-identifier/SKILL.md` |
| `/automation-flow-proposer` | QA Agent | `.opencode/skills/automation-flow-proposer/SKILL.md` |
| `/performance-analyzer` | QA Agent | `.opencode/skills/performance-analyzer/SKILL.md` |

### Instructions (path-scoped)
| Scope | Ruta | Se aplica a |
|---|---|---|
| Backend | `.opencode/instructions/backend.instructions.md` | `**/*.java` |
| Tests | `.opencode/instructions/tests.instructions.md` | `**/test/**/*.java` |

### Lineamientos y Contexto
| Documento | Ruta |
|---|---|
| Lineamientos de Desarrollo | `.opencode/docs/lineamientos/dev-guidelines.md` |
| Lineamientos QA | `.opencode/docs/lineamientos/qa-guidelines.md` |
| Stack + Arquitectura + Naming | `.opencode/instructions/backend.instructions.md` |

### Lineamientos generales para todos los agentes
- **Reglas de Oro**: ver `.opencode/AGENTS.md` — rigen TODAS las interacciones.
- **Specs activas**: `.opencode/specs/` — consultar siempre antes de implementar.

---

## Reglas de Oro

> Principio rector: todas las contribuciones de la IA deben ser seguras, transparentes, con propósito definido y alineadas con las instrucciones explícitas del usuario.

### I. Integridad del Código y del Sistema
- **No código no autorizado**: no escribir, generar ni sugerir código nuevo a menos que el usuario lo solicite explícitamente.
- **No modificaciones no autorizadas**: no modificar, refactorizar ni eliminar código, archivos o estructuras existentes sin aprobación explícita.
- **Preservar la lógica existente**: respetar los patrones arquitectónicos, el estilo de codificación y la lógica operativa existentes del proyecto.

### II. Clarificación de Requisitos
- **Clarificación obligatoria**: si la solicitud es ambigua, incompleta o poco clara, detenerse y solicitar clarificación antes de proceder.
- **No realizar suposiciones**: basar todas las acciones estrictamente en información explícita provista por el usuario.

### III. Transparencia Operativa
- **Explicar antes de actuar**: antes de cualquier acción, explicar qué se hará y posibles implicaciones.
- **Detención ante la incertidumbre**: si surge inseguridad o conflicto con estas reglas, detenerse y consultar al usuario.
- **Acciones orientadas a un propósito**: cada acción debe ser directamente relevante para la solicitud explícita.

---

## Diccionario de Dominio

Términos canónicos a usar en specs, código y mensajes:

| Término | Definición | Sinónimos rechazados |
|---------|-----------|---------------------|
| **Persona** (`Persona`) | Entidad base con datos personales (nombre, género, edad, identificación, dirección, teléfono) | Usuario, individuo |
| **Cliente** (`Cliente`) | Entidad que hereda de Persona. Tiene `clienteId`, contraseña y estado (activo/inactivo) | Usuario, customer |
| **Cuenta** (`Cuenta`) | Producto financiero con número de cuenta, tipo (Ahorro/Corriente), saldo inicial y estado | Account, producto |
| **Movimiento** (`Movimiento`) | Transacción bancaria con fecha, tipo (Depósito/Retiro), valor y saldo resultante | Transacción, operación |
| **Saldo** (`saldo`) | Cantidad monetaria disponible en una cuenta en un momento dado | Balance, disponible |
| **Depósito** | Movimiento con valor positivo que incrementa el saldo | Crédito, abono, ingreso |
| **Retiro** | Movimiento con valor negativo que disminuye el saldo | Débito, egreso, extracción |
| **Estado de Cuenta** | Reporte que contiene cuentas con saldos y detalle de movimientos en un rango de fechas | Extracto, statement |
| **Microservicio** (`ms`) | Servicio independiente con su propia base de datos y responsabilidades acotadas | Módulo, componente |
| **clienteId** | Identificador único del cliente (String, PK en Cliente) | customerId, id |
| **numeroCuenta** | Número único que identifica una cuenta bancaria | accountNumber, nroCuenta |
| **tipoCuenta** | Clasificación de la cuenta: `AHORRO` o `CORRIENTE` | accountType |
| **tipoMovimiento** | Clasificación del movimiento: `DEPOSITO` o `RETIRO` | movementType |

**Reglas:** `clienteId` siempre String. `Cuenta` tiene FK a `clienteId`. `Movimiento` tiene FK a `Cuenta`. Timestamps en `camelCase` (`createdAt`, `updatedAt`). Nombres de tablas en `snake_case` (plural). Nombres de entidades Java en `PascalCase`.

---

## Stack Tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 17+ |
| Framework | Spring Boot 3.x |
| Build Tool | Gradle |
| ORM | JPA / Hibernate |
| Base de Datos | PostgreSQL |
| Mensajería | RabbitMQ |
| Testing | JUnit 5 + Mockito + Spring Boot Test |
| Contenedores | Docker + Docker Compose |
| Paquete Base | `com.sofka.banking` |

### Microservicios

| Microservicio | Puerto | DB | Entidades |
|---|---|---|---|
| `ms-clientes-personas` | 8081 | `db_clientes` | Persona, Cliente |
| `ms-cuentas-movimientos` | 8082 | `db_cuentas` | Cliente (copia), Cuenta, Movimiento |

### Comunicación Asíncrona

| Evento | Emisor | Consumidor | Routing Key |
|---|---|---|---|
| `cliente.creado` | ms-clientes-personas | ms-cuentas-movimientos | `cliente.creado` |
| `cliente.actualizado` | ms-clientes-personas | ms-cuentas-movimientos | `cliente.actualizado` |

Exchange: `banking.exchange` (tipo `topic`)

---

## Project Overview

> Ver `README.md` en la raíz del proyecto.
