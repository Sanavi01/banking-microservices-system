---
id: SPEC-003
status: APPROVED
feature: hu-03-movimientos
created: 2026-04-28
updated: 2026-04-28
author: spec-generator
version: "1.0"
related-specs:
  - SPEC-002 (hu-02-crud-cuentas)
microservicio: ms-cuentas-movimientos
---

# Spec HU-03: Registro de Movimientos con Validación de Saldo

> **Microservicio:** `ms-cuentas-movimientos` (puerto 8082)
> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de implementar.
> **Endpoints:** `/movimientos`

---

## 1. REQUERIMIENTOS

### Historia de Usuario

```
Como:        Cajero del sistema bancario
Quiero:      Registrar depósitos y retiros en cuentas bancarias, con validación de que el saldo sea suficiente para retiros
Para:        Llevar el control de las transacciones y mantener actualizado el saldo disponible

Prioridad:   Alta
Estimación:  M
Dependencias: SPEC-002 — Requiere que la cuenta exista y tenga un saldo disponible
Capa:        Backend (ms-cuentas-movimientos)
```

### Criterios de Aceptación

**Happy Path**
```gherkin
CRITERIO-3.1: Registrar un depósito (valor positivo)
  Dado que:  existe una cuenta con id=1 y saldo actual 2000.00
  Cuando:    envío un POST a /movimientos con {"cuentaId": 1, "valor": 600.00}
  Entonces:  el sistema registra el movimiento como tipo "Depósito"
  Y         actualiza el saldo de la cuenta a 2600.00
  Y         retorna 201 con los datos del movimiento

CRITERIO-3.2: Registrar un retiro (valor negativo) con saldo suficiente
  Dado que:  existe una cuenta con id=1 y saldo actual 2000.00
  Cuando:    envío un POST a /movimientos con {"cuentaId": 1, "valor": -575.00}
  Entonces:  el sistema registra el movimiento como tipo "Retiro"
  Y         actualiza el saldo de la cuenta a 1425.00
  Y         retorna 201 con los datos del movimiento

CRITERIO-3.3: Listar todos los movimientos
  Dado que:  existen movimientos registrados
  Cuando:    envío un GET a /movimientos
  Entonces:  el sistema retorna 200 con la lista de movimientos

CRITERIO-3.4: Obtener un movimiento por ID
  Dado que:  existe un movimiento con id=1
  Cuando:    envío un GET a /movimientos/1
  Entonces:  el sistema retorna 200 con los datos del movimiento
```

**Error Path**
```gherkin
CRITERIO-3.5: Intentar retiro con saldo insuficiente
  Dado que:  existe una cuenta con id=3 y saldo actual 0.00
  Cuando:    envío un POST a /movimientos con {"cuentaId": 3, "valor": -100.00}
  Entonces:  el sistema retorna 422 con mensaje "Saldo no disponible"

CRITERIO-3.6: Registrar movimiento en cuenta inexistente
  Dado que:  no existe una cuenta con id=999
  Cuando:    envío un POST a /movimientos con {"cuentaId": 999, "valor": 100.00}
  Entonces:  el sistema retorna 404 con mensaje "Cuenta no encontrada: 999"

CRITERIO-3.7: Registrar movimiento con valor cero
  Dado que:  existe una cuenta con id=1
  Cuando:    envío un POST a /movimientos con {"cuentaId": 1, "valor": 0.00}
  Entonces:  el sistema retorna 400 con mensaje "El valor del movimiento no puede ser cero"

CRITERIO-3.8: Obtener movimiento inexistente
  Dado que:  no existe un movimiento con id=999
  Cuando:    envío un GET a /movimientos/999
  Entonces:  el sistema retorna 404 con mensaje "Movimiento no encontrado: 999"
```

**Edge Cases**
```gherkin
CRITERIO-3.9: Retiro exacto del saldo total (saldo queda en 0)
  Dado que:  existe una cuenta con id=4 y saldo actual 540.00
  Cuando:    envío un POST a /movimientos con {"cuentaId": 4, "valor": -540.00}
  Entonces:  el sistema registra el movimiento
  Y         actualiza el saldo de la cuenta a 0.00
  Y         retorna 201
```

### Reglas de Negocio

1. `valor > 0` implica un **Depósito** (tipoMovimiento = "Depósito"), `valor < 0` implica un **Retiro** (tipoMovimiento = "Retiro").
2. Al registrar un movimiento, se calcula: `nuevoSaldo = saldoActual + valor` (el valor ya incluye el signo).
3. Si el movimiento es un retiro y `saldoActual + valor < 0` → error HTTP 422 "Saldo no disponible".
4. Cada movimiento se registra con la fecha actual (auto-generada).
5. El campo `saldo` del movimiento almacena el saldo resultante después de aplicar la transacción.
6. El `saldoInicial` de la cuenta NO se modifica; el saldo disponible se calcula como `saldoInicial + SUM(valor de movimientos)`.
7. El movimiento es inmutable una vez creado (no se puede editar ni eliminar).

---

## 2. DISEÑO

### Entidad JPA

#### Movimiento (tabla: movimientos)

```java
@Entity
@Table(name = "movimientos")
@Data
@NoArgsConstructor
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;  // "Depósito" o "Retiro"

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }
}
```

### DTOs

#### MovimientoCreateDTO (Request — POST)

```java
@Data
public class MovimientoCreateDTO {

    @NotNull(message = "El ID de cuenta es obligatorio")
    private Long cuentaId;

    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "-999999999.99")
    @DecimalMax(value = "999999999.99")
    private BigDecimal valor;
}
```

**Nota:** El tipo de movimiento (Depósito/Retiro) se determina automáticamente por el signo del valor. El usuario solo envía `cuentaId` y `valor`. El campo `valor` puede ser positivo (depósito) o negativo (retiro).

#### MovimientoResponseDTO (Response)

```json
{
  "id": 1,
  "fecha": "2026-04-28T10:30:00",
  "tipoMovimiento": "Depósito",
  "valor": 600.00,
  "saldo": 700.00,
  "cuentaId": 2
}
```

### API Endpoints

| Método | Ruta | Descripción | Request | Response |
|--------|------|-------------|---------|----------|
| `POST` | `/movimientos` | Registrar movimiento | `MovimientoCreateDTO` | `201` + `MovimientoResponseDTO` |
| `GET` | `/movimientos` | Listar todos | — | `200` + `List<MovimientoResponseDTO>` |
| `GET` | `/movimientos/{id}` | Obtener por ID | — | `200` + `MovimientoResponseDTO` |

**Códigos de error:**
| Código | Caso | Mensaje |
|--------|------|---------|
| `400` | Datos inválidos o valor = 0 | Mensaje de validación |
| `404` | Cuenta o movimiento no encontrado | "Cuenta no encontrada: {id}" o "Movimiento no encontrado: {id}" |
| `422` | Saldo insuficiente | **"Saldo no disponible"** |

### Ejemplos de Request/Response

**POST /movimientos — Request (Depósito):**
```json
{
  "cuentaId": 2,
  "valor": 600.00
}
```

**POST /movimientos — Response 201 (Depósito):**
```json
{
  "id": 1,
  "fecha": "2026-04-28T10:30:00",
  "tipoMovimiento": "Depósito",
  "valor": 600.00,
  "saldo": 700.00,
  "cuentaId": 2
}
```

**POST /movimientos — Request (Retiro):**
```json
{
  "cuentaId": 1,
  "valor": -575.00
}
```

**POST /movimientos — Response 201 (Retiro):**
```json
{
  "id": 2,
  "fecha": "2026-04-28T10:35:00",
  "tipoMovimiento": "Retiro",
  "valor": -575.00,
  "saldo": 1425.00,
  "cuentaId": 1
}
```

**POST /movimientos — Response 422 (Saldo insuficiente):**
```json
{
  "message": "Saldo no disponible",
  "status": 422
}
```

### Lógica de Negocio (MovimientoService)

```java
@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    public MovimientoResponseDTO registrarMovimiento(MovimientoCreateDTO dto) {
        // 1. Validar que el valor no sea cero
        if (dto.getValor().compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidMovementException("El valor del movimiento no puede ser cero");
        }

        // 2. Buscar cuenta
        Cuenta cuenta = cuentaRepository.findById(dto.getCuentaId())
            .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + dto.getCuentaId()));

        // 3. Calcular saldo actual = saldoInicial + SUM(movimientos)
        BigDecimal saldoActual = movimientoRepository
            .sumValorByCuentaId(cuenta.getId())
            .orElse(BigDecimal.ZERO)
            .add(cuenta.getSaldoInicial());

        // 4. Validar saldo para retiros
        BigDecimal nuevoSaldo = saldoActual.add(dto.getValor());
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException("Saldo no disponible");
        }

        // 5. Crear movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setCuenta(cuenta);
        movimiento.setValor(dto.getValor());
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setTipoMovimiento(dto.getValor().compareTo(BigDecimal.ZERO) > 0 ? "Depósito" : "Retiro");

        Movimiento saved = movimientoRepository.save(movimiento);
        return toDTO(saved);
    }
}
```

### Capas de Implementación

```
src/main/java/com/sofka/banking/cuentas/
├── entity/
│   ├── Cuenta.java              (ya existente de HU-02)
│   └── Movimiento.java          (nuevo)
├── dto/
│   ├── MovimientoCreateDTO.java (nuevo)
│   └── MovimientoResponseDTO.java (nuevo)
├── repository/
│   ├── CuentaRepository.java    (ya existente)
│   └── MovimientoRepository.java (nuevo)
├── service/
│   └── MovimientoService.java   (nuevo)
├── controller/
│   └── MovimientoController.java (nuevo)
└── exception/
    ├── GlobalExceptionHandler.java  (extender con nuevos handlers)
    ├── InsufficientBalanceException.java  (nuevo, HTTP 422)
    └── InvalidMovementException.java      (nuevo, HTTP 400)
```

---

## 3. PLAN DE PRUEBAS

### 3A. Pruebas Unitarias (Código)

| # | Clase | Método | Descripción |
|---|-------|--------|-------------|
| UT-3.1 | `MovimientoServiceTest` | `registrarMovimiento_shouldCreateDeposito_whenValorPositivo()` | Valor positivo crea tipo "Depósito" |
| UT-3.2 | `MovimientoServiceTest` | `registrarMovimiento_shouldCreateRetiro_whenValorNegativoYSaldoSuficiente()` | Valor negativo con saldo suficiente crea "Retiro" |
| UT-3.3 | `MovimientoServiceTest` | `registrarMovimiento_shouldThrowSaldoNoDisponible_whenSaldoInsuficiente()` | Retiro sin saldo lanza InsufficientBalanceException con mensaje "Saldo no disponible" |
| UT-3.4 | `MovimientoServiceTest` | `registrarMovimiento_shouldUpdateSaldoCorrectly_afterDeposito()` | Depósito actualiza el saldo = saldoInicial + SUM(valores) |
| UT-3.5 | `MovimientoServiceTest` | `registrarMovimiento_shouldUpdateSaldoCorrectly_afterRetiro()` | Retiro actualiza el saldo correctamente |
| UT-3.6 | `MovimientoServiceTest` | `registrarMovimiento_shouldAllowRetiroExacto_totalSaldo()` | Retiro del saldo total (saldo queda 0) es válido |
| UT-3.7 | `MovimientoServiceTest` | `registrarMovimiento_shouldThrow_whenCuentaNotFound()` | Cuenta inexistente lanza ResourceNotFoundException |
| UT-3.8 | `MovimientoServiceTest` | `registrarMovimiento_shouldThrow_whenValorCero()` | Valor = 0 lanza InvalidMovementException |
| UT-3.9 | `MovimientoServiceTest` | `registrarMovimiento_shouldSetFechaActual()` | La fecha se asigna automáticamente con LocalDateTime.now() |
| UT-3.10 | `MovimientoServiceTest` | `findById_shouldReturnDto_whenExists()` | Buscar movimiento por ID retorna DTO |
| UT-3.11 | `MovimientoServiceTest` | `findById_shouldThrow_whenNotFound()` | Movimiento inexistente lanza ResourceNotFoundException |
| UT-3.12 | `MovimientoServiceTest` | `findAll_shouldReturnList()` | Listar todos retorna lista de DTOs |
| UT-3.13 | `MovimientoRepositoryTest` | `save_shouldPersistMovimiento()` | save() persiste el movimiento con fecha y saldo |
| UT-3.14 | `MovimientoRepositoryTest` | `sumValorByCuentaId_shouldReturnSum()` | Query de suma de valores por cuenta retorna el total correcto |

### 3B. Pruebas de Integración (Endpoints)

| # | Método | Endpoint | Escenario | Esperado |
|---|--------|----------|-----------|----------|
| IT-3.1 | `POST` | `/movimientos` | `{"cuentaId": 1, "valor": 600.00}` sobre cuenta con saldo | `201` + "tipoMovimiento": "Depósito", saldo actualizado |
| IT-3.2 | `POST` | `/movimientos` | `{"cuentaId": 1, "valor": -575.00}` con saldo suficiente | `201` + "tipoMovimiento": "Retiro", saldo actualizado |
| IT-3.3 | `POST` | `/movimientos` | `{"cuentaId": 3, "valor": -100.00}` con saldo insuficiente (0) | `422` + mensaje "Saldo no disponible" |
| IT-3.4 | `POST` | `/movimientos` | `{"cuentaId": 4, "valor": -540.00}` retiro del saldo total (540) | `201` + saldo=0.00 |
| IT-3.5 | `POST` | `/movimientos` | `{"cuentaId": 999, "valor": 100.00}` cuenta inexistente | `404` + "Cuenta no encontrada: 999" |
| IT-3.6 | `POST` | `/movimientos` | `{"cuentaId": 1, "valor": 0.00}` valor cero | `400` + mensaje de validación |
| IT-3.7 | `POST` | `/movimientos` | Sin campo `cuentaId` | `400` + mensaje "El ID de cuenta es obligatorio" |
| IT-3.8 | `POST` | `/movimientos` | Sin campo `valor` | `400` + mensaje "El valor es obligatorio" |
| IT-3.9 | `GET` | `/movimientos` | Sin parámetros | `200` + array JSON de movimientos |
| IT-3.10 | `GET` | `/movimientos/{id}` | ID existente | `200` + JSON del movimiento |
| IT-3.11 | `GET` | `/movimientos/{id}` | ID inexistente | `404` + "Movimiento no encontrado" |
| IT-3.12 | `POST` | `/movimientos` | Múltiples movimientos consecutivos — verificar saldo acumulado | Cada uno actualiza el saldo correctamente |

### 3C. Validación de Casos de Uso del Ejercicio

Datos del ejercicio (sección 4.3):
| Cuenta | Movimiento | Valor | Saldo Esperado |
|--------|-----------|-------|----------------|
| 478758 (id=1) | Retiro | -575 | 1425 |
| 225487 (id=2) | Depósito | +600 | 700 |
| 495878 (id=3) | Depósito | +150 | 150 |
| 496825 (id=4) | Retiro | -540 | 0 |

Pruebas de validación de estos casos:
| # | Test | Validación |
|---|------|-----------|
| CV-3.1 | IT-3.2: Retiro 575 de cuenta con saldo inicial 2000 | `saldo` en response = 1425 |
| CV-3.2 | IT-3.1: Depósito 600 en cuenta con saldo inicial 100 | `saldo` en response = 700 |
| CV-3.3 | Depósito 150 en cuenta con saldo inicial 0 | `saldo` en response = 150 |
| CV-3.4 | IT-3.4: Retiro 540 de cuenta con saldo inicial 540 (total) | `saldo` en response = 0 |

---

## 4. TAREAS DE IMPLEMENTACIÓN

### Backend
- [x] Crear entidad `Movimiento`
- [x] Implementar `MovimientoCreateDTO`, `MovimientoResponseDTO`
- [x] Implementar `MovimientoRepository` con query `sumValorByCuentaId`
- [x] Implementar `MovimientoService` con lógica de registro + validación de saldo
- [x] Implementar `MovimientoController` con GET (listar, por ID) y POST (registrar)
- [x] Implementar `InsufficientBalanceException` (HTTP 422)
- [x] Implementar `InvalidMovementException` (HTTP 400)
- [x] Extender `GlobalExceptionHandler` con handlers para las nuevas excepciones

### Pruebas Unitarias
- [x] `MovimientoServiceTest` — 12 tests (UT-3.1 a UT-3.12)
- [x] `MovimientoRepositoryTest` — 2 tests (UT-3.13, UT-3.14)

### Pruebas de Integración
- [x] `MovimientoControllerIntegrationTest` — 12 tests (IT-3.1 a IT-3.12)


