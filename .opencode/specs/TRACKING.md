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
| **Spec** | `[x]` DRAFT → `[ ]` APPROVED |
| **Implementación** | `[x]` IN_PROGRESS → `[x]` COMPLETED |
| **Tests Unitarios** | `[ ]` PENDING → `[ ]` PASSING |
| **Tests Integración** | `[ ]` PENDING → `[ ]` PASSING |
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
- [ ] `ClienteServiceTest` (11 tests)
- [ ] `ClienteRepositoryTest` (2 tests)
- [ ] `ClienteControllerIntegrationTest` (14 tests)

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
| **Spec** | `[ ]` DRAFT → `[ ]` APPROVED |
| **Implementación** | `[ ]` IN_PROGRESS → `[ ]` COMPLETED |
| **Tests Unitarios** | `[ ]` PENDING → `[ ]` PASSING |
| **Tests Integración** | `[ ]` PENDING → `[ ]` PASSING |
| **QA** | `[ ]` PENDING → `[ ]` COMPLETED |

#### Checklist Implementación
- [ ] Entidad JPA: `Movimiento`
- [ ] DTOs: `MovimientoCreateDTO`, `MovimientoResponseDTO`
- [ ] Repositorio: `MovimientoRepository` (con query `sumValorByCuentaId`)
- [ ] Service: `MovimientoService` (validación saldo + registro)
- [ ] Controller: `MovimientoController` (POST + GET)
- [ ] Excepciones: `InsufficientBalanceException`, `InvalidMovementException`

#### Checklist Tests
- [ ] `MovimientoServiceTest` (12 tests)
- [ ] `MovimientoRepositoryTest` (2 tests)
- [ ] `MovimientoControllerIntegrationTest` (12 tests)

---

### SPEC-004 — HU-04: Reporte Estado de Cuenta

| Fase | Estado |
|------|--------|
| **Spec** | `[ ]` DRAFT → `[ ]` APPROVED |
| **Implementación** | `[ ]` IN_PROGRESS → `[ ]` COMPLETED |
| **Tests Unitarios** | `[ ]` PENDING → `[ ]` PASSING |
| **Tests Integración** | `[ ]` PENDING → `[ ]` PASSING |
| **QA** | `[ ]` PENDING → `[ ]` COMPLETED |

#### Checklist Implementación
- [ ] DTOs: `ReporteRequest`, `ReporteResponseDTO` (con `@JsonProperty` español)
- [ ] Query JPQL en `MovimientoRepository`: filtro por clienteId + rango fechas
- [ ] Service: `ReporteService` (filtro + validación fechas + mapeo)
- [ ] Controller: `ReporteController` (GET `/reportes`)
- [ ] Excepción: `InvalidDateRangeException`

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
| SPEC-003 | ms-cuentas-movimientos | Movimientos | `[ ]` | `[ ]` (14) | `[ ]` (12) | `[ ]` |
| SPEC-004 | ms-cuentas-movimientos | Reportes | `[ ]` | `[ ]` (9) | `[ ]` (9) | `[ ]` |
| **Totales** | | | | **48 tests** | **49 tests** | |
