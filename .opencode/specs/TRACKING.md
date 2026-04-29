# Tracking de Implementación — Sistema Bancario Microservicios

> Marca con `[x]` cada tarea al completarla.

---

## Fase 0 — Infraestructura

- [ ] `BaseDatos.sql` — script de base de datos con esquema y datos de prueba
- [ ] `docker-compose.yml` — PostgreSQL (x2), RabbitMQ, ms-clientes-personas, ms-cuentas-movimientos

---

## ms-clientes-personas (puerto 8081)

### SPEC-001 — HU-01: CRUD Clientes

| Fase | Estado |
|------|--------|
| **Spec** | `[x]` DRAFT → `[x]` APPROVED |
| **Implementación** | `[x]` IN_PROGRESS → `[x]` COMPLETED |
| **Tests Unitarios** | `[x]` PENDING → `[x]` PASSING |
| **Tests Integración** | `[x]` PENDING → `[x]` PASSING |
| **QA** | `[ ]` PENDING → `[ ]` COMPLETED |

#### Checklist Implementación
- [x] Entidades JPA: `Persona`, `Cliente` (herencia JOINED)
- [x] DTOs: `ClienteCreateDTO`, `ClienteUpdateDTO`, `ClientePatchDTO`, `ClienteResponseDTO`
- [x] Repositorios: `PersonaRepository`, `ClienteRepository`
- [x] Service: `ClienteService` (interfaz) + `ClienteServiceImpl` + `PasswordService`
- [x] Mapper: `ClienteMapper` (Entity ↔ DTO)
- [x] Controller: `ClienteController` (GET/POST/PUT/PATCH/DELETE)
- [x] RabbitMQ Producer: `ClienteEventPublisher` + `ClienteEventDTO`
- [x] RabbitMQ Config: `RabbitMQConfig` (exchange + JSON converter)
- [x] Excepciones: `GlobalExceptionHandler`, `ResourceNotFoundException`, `DuplicateResourceException`
- [x] Config: `application.yml` (PostgreSQL + RabbitMQ)

#### Checklist Tests
- [x] `ClienteServiceTest` (11 tests)
- [x] `ClienteRepositoryTest` (2 tests)
- [x] `ClienteControllerIntegrationTest` (14 tests)

---

## ms-cuentas-movimientos (puerto 8082)

### SPEC-002 — HU-02: CRUD Cuentas

| Fase | Estado |
|------|--------|
| **Spec** | `[ ]` DRAFT → `[ ]` APPROVED |
| **Implementación** | `[ ]` IN_PROGRESS → `[ ]` COMPLETED |
| **Tests Unitarios** | `[ ]` PENDING → `[ ]` PASSING |
| **Tests Integración** | `[ ]` PENDING → `[ ]` PASSING |
| **QA** | `[ ]` PENDING → `[ ]` COMPLETED |

#### Checklist Implementación
- [ ] Entidad JPA: `Cliente` (copia local), `Cuenta`
- [ ] DTOs: `CuentaCreateDTO`, `CuentaUpdateDTO`, `CuentaPatchDTO`, `CuentaResponseDTO`
- [ ] Repositorios: `ClienteRepository`, `CuentaRepository`
- [ ] RabbitMQ Consumer: `ClienteEventConsumer`
- [ ] Service: `CuentaService` (CRUD + validaciones)
- [ ] Controller: `CuentaController` (GET/POST/PUT/PATCH/DELETE)

#### Checklist Tests
- [ ] `CuentaServiceTest` (9 tests)
- [ ] `CuentaRepositoryTest` (2 tests)
- [ ] `ClienteEventConsumerTest` (1 test)
- [ ] `CuentaControllerIntegrationTest` (14 tests)

---

### SPEC-003 — HU-03: Movimientos + Validación Saldo

| Fase | Estado |
|------|--------|
| **Spec** | `[x]` DRAFT → `[x]` APPROVED |
| **Implementación** | `[x]` IN_PROGRESS → `[x]` COMPLETED |
| **Tests Unitarios** | `[x]` PENDING → `[x]` PASSING |
| **Tests Integración** | `[x]` PENDING → `[x]` PASSING |
| **QA** | `[ ]` PENDING → `[ ]` COMPLETED |

#### Checklist Implementación
- [x] Entidad JPA: `Movimiento`
- [x] DTOs: `MovimientoCreateDTO`, `MovimientoResponseDTO`
- [x] Mapper: `MovimientoMapper` (SRP)
- [x] Repositorio: `MovimientoRepository` (con query `sumValorByCuentaId`)
- [x] Service: `MovimientoService` (interfaz) + `MovimientoServiceImpl` (validación saldo + registro)
- [x] Controller: `MovimientoController` (POST + GET)
- [x] Excepciones: `InsufficientBalanceException` (422), `InvalidMovementException` (400)
- [x] GlobalExceptionHandler extendido con nuevos handlers
- [x] Strategy Pattern: `TipoMovimientoStrategy`, `DepositoStrategy`, `RetiroStrategy`, `TipoMovimientoResolver`

#### Checklist Tests
- [x] `MovimientoServiceTest` (12 tests)
- [x] `MovimientoRepositoryTest` (2 tests)
- [x] `MovimientoControllerIntegrationTest` (12 tests)

---

### SPEC-004 — HU-04: Reporte Estado de Cuenta

| Fase | Estado |
|------|--------|
| **Spec** | `[ ]` DRAFT → `[ ]` APPROVED |
| **Implementación** | `[x]` IN_PROGRESS → `[x]` COMPLETED |
| **Tests Unitarios** | `[x]` PENDING → `[x]` PASSING |
| **Tests Integración** | `[x]` PENDING → `[x]` PASSING |
| **QA** | `[ ]` PENDING → `[ ]` COMPLETED |

#### Checklist Implementación
- [x] DTOs: `ReporteRequest`, `ReporteResponseDTO` (con `@JsonProperty` español)
- [x] Query JPQL en `MovimientoRepository`: filtro por clienteId + rango fechas
- [x] Service: `ReporteService` (filtro + validación fechas + mapeo)
- [x] Controller: `ReporteController` (GET `/reportes`)
- [x] Excepción: `InvalidDateRangeException`

#### Checklist Tests
- [ ] `ReporteServiceTest` (8 tests)
- [ ] `MovimientoRepositoryTest` (1 test)
- [ ] `ReporteControllerIntegrationTest` (9 tests)

---

## Resumen General

| Spec | Microservicio | HU | Implementación | Tests UT | Tests IT | QA |
|------|--------------|----|---------------|----------|---------|----|
| SPEC-001 | ms-clientes-personas | CRUD Clientes | `[ ]` | `[ ]` (13) | `[ ]` (14) | `[ ]` |
| SPEC-002 | ms-cuentas-movimientos | CRUD Cuentas | `[ ]` | `[ ]` (12) | `[ ]` (14) | `[ ]` |
| SPEC-003 | ms-cuentas-movimientos | Movimientos | `[x]` | `[x]` (14) | `[x]` (12) | `[ ]` |
| SPEC-004 | ms-cuentas-movimientos | Reportes | `[x]` | `[x]` (9) | `[x]` (9) | `[ ]` |
| **Totales** | | | | **48 tests** | **49 tests** | |
