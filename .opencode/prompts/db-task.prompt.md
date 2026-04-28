---
description: 'Ejecuta el Database Agent para diseñar esquemas de datos, generar scripts de migración, seeders y optimizar queries a partir de la spec aprobada.'
agent: Database Agent
---

Ejecuta el Database Agent (MARCO DB) para diseñar y gestionar el modelo de persistencia del feature.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

**Instrucciones para @Database Agent:**

1. Lee `.opencode/instructions/backend.instructions.md` — confirma el motor de BD aprobado
2. Lee `.opencode/docs/lineamientos/dev-guidelines.md`
3. Lee la **Sección 2 — DISEÑO — Modelos de Datos** de `.opencode/specs/${input:featureName}.spec.md`
4. Escanea entidades y repositorios existentes en `src/main/java/com/sofka/banking/` (carpetas `model/entity/` y `repository/`)
5. Ejecuta el flujo completo:
   - Diseña o actualiza el esquema de datos (entidades, campos, relaciones, índices)
   - Genera entidad JPA: `src/main/java/.../model/entity/<Feature>.java` con `@Entity`, `@Table`
   - Genera migración Flyway/Liquibase: `src/main/resources/db/migration/V<version>__<descripcion>.sql`
   - Genera seeder SQL con datos de prueba: `src/test/resources/data/seed_<feature>.sql`
   - Registra ADR si hay decisiones de diseño relevantes
6. Presenta reporte consolidado de cambios al modelo de datos

**Prerequisito:** Debe existir `.opencode/specs/${input:featureName}.spec.md` con estado APPROVED y Sección 2 completa. Si no, ejecutar `/generate-spec` primero.

**Nota:** Ejecutar ANTES o en paralelo con el Backend Developer para que los contratos de persistencia estén definidos antes de implementar los repositorios.
