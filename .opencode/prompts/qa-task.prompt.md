---
description: 'Ejecuta el QA Agent con los skills secuenciales para generar el plan de calidad completo basado en la spec aprobada.'
agent: QA Agent
---

Ejecuta el QA Agent completo con los skills en secuencia.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

**Instrucciones para @QA Agent:**

1. Lee `.opencode/docs/lineamientos/qa-guidelines.md` como primer paso
2. Lee la spec en `.opencode/specs/${input:featureName}.spec.md`
3. Ejecuta los skills en orden:
   - SKILL 1: `/gherkin-case-generator`   → `docs/output/qa/features/`
   - SKILL 2: `/risk-identifier`          → `docs/output/qa/risk-matrix.md`
   - SKILL 3: `/automation-flow-proposer` → `docs/output/qa/automation-roadmap.md`
   - SKILL 4: `/performance-analyzer`    → `docs/output/qa/performance-plan.md`
4. Genera reporte consolidado al finalizar

**Prerequisito:** Debe existir `.opencode/specs/${input:featureName}.spec.md` con estado APPROVED. Si no, ejecutar `/generate-spec` primero.
