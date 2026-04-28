---
name: implement-backend
description: Implementa un feature completo en el backend. Requiere spec con status APPROVED en .opencode/specs/.
argument-hint: "<nombre-feature>"
---

# Implement Backend

## Prerequisitos
1. Leer spec: `.opencode/specs/<feature>.spec.md` — sección 2 (modelos, endpoints)
2. Leer stack: `.opencode/instructions/backend.instructions.md`
3. Leer arquitectura: `.opencode/instructions/backend.instructions.md`

## Orden de implementación
```
entities → repositories → services → controllers → registrar en punto de entrada
```

| Capa | Responsabilidad |
|------|-----------------|
| **Entities / DTOs** | Entidades JPA (`@Entity`) y DTOs con validaciones (`@Valid`) |
| **Repositories** | Acceso a DB mediante Spring Data JPA — sin lógica de negocio |
| **Services** | Lógica de negocio pura — orquesta repositorios |
| **Controllers / Routes** | Endpoints REST (`@RestController`) + DI + delegar al service |

## Patrón de DI (obligatorio)
- Inyectar dependencias con constructor injection (`@RequiredArgsConstructor` o `@Autowired`)
- El service recibe el repositorio por constructor; el controller recibe el service por constructor

Ver patrones específicos del stack en `.opencode/instructions/backend.instructions.md`.

## Reglas
Ver `.claude/rules/backend.md` — async, naming, errores, timestamps.

## Restricciones
- Solo directorio de backend del proyecto. No tocar frontend.
- No generar tests (responsabilidad de `test-engineer-backend`).
