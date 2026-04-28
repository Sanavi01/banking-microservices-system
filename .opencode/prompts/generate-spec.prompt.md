---
name: generate-spec
description: Genera una especificación técnica ASDD para un nuevo feature. Usa este comando con el nombre e descripción del feature.
argument-hint: "<nombre-feature>: <descripción del requerimiento>"
agent: Spec Generator
tools:
  - edit/createFile
  - read/readFile
  - search/listDirectory
  - search
---

Genera una especificación técnica completa en `.opencode/specs/` para el siguiente requerimiento.

**Feature**: ${input:featureName:nombre del feature en kebab-case}
**Requerimiento**: ${input:requirement:descripción del requerimiento — o "ver requirements" para cargar desde .opencode/requirements/}

## Pasos a seguir:

1. **Si el requerimiento no se proporcionó**, busca en `.opencode/requirements/${input:featureName}.md`. Si existe, úsalo como fuente.
2. Lee el stack: `.opencode/instructions/backend.instructions.md`.
3. Explora el código existente para identificar patrones, modelos y rutas relacionadas.
4. Genera la spec usando la plantilla en `.opencode/skills/generate-spec/spec-template.md`.
5. Guarda el archivo como `.opencode/specs/${input:featureName}.spec.md` con estado `DRAFT`.
6. Confirma la creación con un resumen de la spec al usuario.

## La spec debe cubrir:
- Historias de usuario con criterios de aceptación en Gherkin
- Modelos de datos (JPA Entities + PostgreSQL)
- Endpoints de API con request/response y errores
- Plan de pruebas (backlog de tasks Backend + QA)
