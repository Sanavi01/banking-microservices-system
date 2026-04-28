---
id: SPEC-002
status: DRAFT
feature: hu-02-crud-cuentas
created: 2026-04-28
updated: 2026-04-28
author: spec-generator
version: "1.0"
related-specs:
  - SPEC-001 (hu-01-crud-clientes)
microservicio: ms-cuentas-movimientos
---

# Spec HU-02: CRUD de Cuentas

> **Microservicio:** `ms-cuentas-movimientos` (puerto 8082)
> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de implementar.
> **Endpoints:** `/cuentas`

---

## 1. REQUERIMIENTOS

### Historia de Usuario

```
Como:        Cajero del sistema bancario
Quiero:      Crear, consultar, editar y eliminar cuentas bancarias asociadas a clientes
Para:        Administrar los productos financieros de cada cliente

Prioridad:   Alta
Estimación:  L
Dependencias: SPEC-001 — Requiere que el cliente exista en la copia local (sincronizado vía RabbitMQ desde ms-clientes-personas)
Capa:        Backend (ms-cuentas-movimientos)
```

### Criterios de Aceptación

**Happy Path**
```gherkin
CRITERIO-2.1: Crear una cuenta asociada a un cliente existente
  Dado que:  existe un cliente con clienteId "CLI-001" en la BD local
  Cuando:    envío un POST a /cuentas con numeroCuenta, tipoCuenta, saldoInicial, estado, clienteId
  Entonces:  el sistema crea la cuenta y retorna 201 con los datos completos

CRITERIO-2.2: Listar todas las cuentas
  Dado que:  existen cuentas registradas
  Cuando:    envío un GET a /cuentas
  Entonces:  el sistema retorna 200 con la lista de cuentas

CRITERIO-2.3: Obtener cuenta por ID
  Dado que:  existe una cuenta con id=1
  Cuando:    envío un GET a /cuentas/1
  Entonces:  el sistema retorna 200 con los datos de la cuenta

CRITERIO-2.4: Actualizar cuenta (PUT)
  Dado que:  existe una cuenta con id=1
  Cuando:    envío un PUT a /cuentas/1 con nuevos datos
  Entonces:  el sistema actualiza y retorna 200

CRITERIO-2.5: Actualizar parcialmente cuenta (PATCH)
  Dado que:  existe una cuenta con id=1 y estado true
  Cuando:    envío un PATCH a /cuentas/1 con {"estado": false}
  Entonces:  el sistema actualiza solo el campo estado y retorna 200

CRITERIO-2.6: Eliminar cuenta (DELETE)
  Dado que:  existe una cuenta con id=1
  Cuando:    envío un DELETE a /cuentas/1
  Entonces:  el sistema elimina la cuenta y retorna 204
```

**Error Path**
```gherkin
CRITERIO-2.7: Crear cuenta sin cliente existente
  Dado que:  no existe un cliente con clienteId "XYZ-999" en la BD local
  Cuando:    envío un POST a /cuentas con clienteId "XYZ-999"
  Entonces:  el sistema retorna 404 con mensaje "Cliente no encontrado: XYZ-999"

CRITERIO-2.8: Crear cuenta con número duplicado
  Dado que:  ya existe una cuenta con numeroCuenta "478758"
  Cuando:    envío un POST a /cuentas con numeroCuenta "478758"
  Entonces:  el sistema retorna 409 con mensaje "Ya existe una cuenta con número: 478758"

CRITERIO-2.9: Obtener cuenta inexistente
  Dado que:  no existe una cuenta con id=999
  Cuando:    envío un GET a /cuentas/999
  Entonces:  el sistema retorna 404 con mensaje "Cuenta no encontrada: 999"

CRITERIO-2.10: Actualizar/Eliminar cuenta inexistente
  Dado que:  no existe una cuenta con id=999
  Cuando:    envío un PUT, PATCH o DELETE a /cuentas/999
  Entonces:  el sistema retorna 404 con mensaje "Cuenta no encontrada: 999"
```

### Reglas de Negocio

1. Toda cuenta debe estar asociada a un cliente existente en la BD local.
2. `numeroCuenta` es único en todo el sistema.
3. `tipoCuenta` acepta valores: "Ahorro" o "Corriente".
4. `saldoInicial` se usa como saldo de apertura; el saldo real se calcula con los movimientos.
5. `estado` indica si la cuenta está activa (true) o inactiva (false).
6. `clienteId` referencia al cliente en la copia local (sincronizado vía RabbitMQ).
7. La eliminación es física (DELETE hard).

---

## 2. DISEÑO

### Entidades JPA

#### Cliente (copia local — tabla: clientes)

```java
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
public class Cliente {

    @Id
    @Column(name = "cliente_id", length = 36)
    private String clienteId;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false)
    private Boolean estado;
}
```

**Nota:** Esta es una copia reducida del Cliente del ms-clientes-personas. Se sincroniza vía RabbitMQ.

#### Cuenta (tabla: cuentas)

```java
@Entity
@Table(name = "cuentas")
@Data
@NoArgsConstructor
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 20)
    @NotBlank
    private String numeroCuenta;

    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    @NotBlank
    private String tipoCuenta;

    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal saldoInicial;

    @Column(nullable = false)
    @NotNull
    private Boolean estado;

    @Column(name = "cliente_id", nullable = false, length = 36)
    @NotBlank
    private String clienteId;
}
```

### DTOs

#### CuentaCreateDTO (Request — POST)

```java
@Data
public class CuentaCreateDTO {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(max = 20)
    private String numeroCuenta;

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    @Pattern(regexp = "^(Ahorro|Corriente)$", message = "Tipo debe ser Ahorro o Corriente")
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo no puede ser negativo")
    private BigDecimal saldoInicial;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;

    @NotBlank(message = "El clienteId es obligatorio")
    private String clienteId;
}
```

#### CuentaUpdateDTO (Request — PUT)

```java
@Data
public class CuentaUpdateDTO {
    @Size(max = 20)
    private String numeroCuenta;
    @Pattern(regexp = "^(Ahorro|Corriente)$")
    private String tipoCuenta;
    @DecimalMin("0.0")
    private BigDecimal saldoInicial;
    private Boolean estado;
    private String clienteId;
}
```

#### CuentaPatchDTO (Request — PATCH)

```java
@Data
public class CuentaPatchDTO {
    private String tipoCuenta;
    private BigDecimal saldoInicial;
    private Boolean estado;
}
```

#### CuentaResponseDTO (Response)

```json
{
  "id": 1,
  "numeroCuenta": "478758",
  "tipoCuenta": "Ahorro",
  "saldoInicial": 2000.00,
  "estado": true,
  "clienteId": "a1b2c3d4-..."
}
```

### API Endpoints

| Método | Ruta | Descripción | Request | Response |
|--------|------|-------------|---------|----------|
| `POST` | `/cuentas` | Crear cuenta | `CuentaCreateDTO` | `201` + `CuentaResponseDTO` |
| `GET` | `/cuentas` | Listar todas | — | `200` + `List<CuentaResponseDTO>` |
| `GET` | `/cuentas/{id}` | Obtener por ID | — | `200` + `CuentaResponseDTO` |
| `PUT` | `/cuentas/{id}` | Actualizar completo | `CuentaUpdateDTO` | `200` + `CuentaResponseDTO` |
| `PATCH` | `/cuentas/{id}` | Actualizar parcial | `CuentaPatchDTO` | `200` + `CuentaResponseDTO` |
| `DELETE` | `/cuentas/{id}` | Eliminar | — | `204` No Content |

**Códigos de error:**
| Código | Caso | Mensaje |
|--------|------|---------|
| `400` | Datos inválidos | Mensaje de validación del campo |
| `404` | Cuenta no encontrada | "Cuenta no encontrada: {id}" |
| `404` | Cliente no encontrado | "Cliente no encontrado: {clienteId}" |
| `409` | Número de cuenta duplicado | "Ya existe una cuenta con número: {numeroCuenta}" |

**POST /cuentas — Request:**
```json
{
  "numeroCuenta": "478758",
  "tipoCuenta": "Ahorro",
  "saldoInicial": 2000.00,
  "estado": true,
  "clienteId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**POST /cuentas — Response 201:**
```json
{
  "id": 1,
  "numeroCuenta": "478758",
  "tipoCuenta": "Ahorro",
  "saldoInicial": 2000.00,
  "estado": true,
  "clienteId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**PATCH /cuentas/1 — Request:**
```json
{
  "estado": false
}
```

**PATCH /cuentas/1 — Response 200:**
```json
{
  "id": 1,
  "numeroCuenta": "478758",
  "tipoCuenta": "Ahorro",
  "saldoInicial": 2000.00,
  "estado": false,
  "clienteId": "a1b2c3d4-..."
}
```

### Capas de Implementación

```
src/main/java/com/sofka/banking/cuentas/
├── entity/
│   ├── Cliente.java          (copia local)
│   └── Cuenta.java
├── dto/
│   ├── CuentaCreateDTO.java
│   ├── CuentaUpdateDTO.java
│   ├── CuentaPatchDTO.java
│   └── CuentaResponseDTO.java
├── repository/
│   ├── CuentaRepository.java
│   └── ClienteRepository.java
├── service/
│   └── CuentaService.java
├── controller/
│   └── CuentaController.java
├── messaging/
│   └── ClienteEventConsumer.java   (escucha cliente.creado y cliente.actualizado)
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
└── config/
    └── RabbitMQConfig.java
```

### RabbitMQ Consumer

```java
@Component
public class ClienteEventConsumer {

    @RabbitListener(queues = "cliente.queue")
    public void handleClienteEvent(ClienteEventDTO event) {
        if ("CLIENTE_CREADO".equals(event.getEvento())) {
            // Replicar cliente en BD local
        } else if ("CLIENTE_ACTUALIZADO".equals(event.getEvento())) {
            // Actualizar copia local del cliente
        }
    }
}
```

---

## 3. PLAN DE PRUEBAS

### 3A. Pruebas Unitarias (Código)

| # | Clase | Método | Descripción |
|---|-------|--------|-------------|
| UT-2.1 | `CuentaServiceTest` | `create_shouldReturnDto_whenValidData()` | Crear cuenta con datos válidos retorna DTO |
| UT-2.2 | `CuentaServiceTest` | `create_shouldThrow_whenClienteNotFound()` | Crear cuenta con clienteId inexistente lanza ResourceNotFoundException |
| UT-2.3 | `CuentaServiceTest` | `create_shouldThrow_whenNumeroCuentaDuplicado()` | Crear cuenta con número existente lanza DuplicateResourceException |
| UT-2.4 | `CuentaServiceTest` | `findById_shouldReturnDto_whenExists()` | Buscar por ID existente retorna DTO |
| UT-2.5 | `CuentaServiceTest` | `findById_shouldThrow_whenNotFound()` | Buscar ID inexistente lanza ResourceNotFoundException |
| UT-2.6 | `CuentaServiceTest` | `findAll_shouldReturnList()` | Listar todas retorna lista de DTOs |
| UT-2.7 | `CuentaServiceTest` | `update_shouldUpdateAllFields()` | PUT actualiza todos los campos |
| UT-2.8 | `CuentaServiceTest` | `patch_shouldUpdateOnlyProvidedFields()` | PATCH actualiza solo campos enviados |
| UT-2.9 | `CuentaServiceTest` | `delete_shouldRemoveCuenta()` | DELETE elimina la cuenta |
| UT-2.10 | `CuentaRepositoryTest` | `save_shouldPersistCuenta()` | save() persiste y retorna entidad con ID |
| UT-2.11 | `CuentaRepositoryTest` | `findByNumeroCuenta_shouldReturnCuenta()` | findByNumeroCuenta retorna entidad |
| UT-2.12 | `ClienteEventConsumerTest` | `shouldReplicateCliente_onClienteCreadoEvent()` | Evento cliente.creado replica cliente |

### 3B. Pruebas de Integración (Endpoints)

| # | Método | Endpoint | Escenario | Esperado |
|---|--------|----------|-----------|----------|
| IT-2.1 | `POST` | `/cuentas` | Body válido con cliente existente | `201` + JSON con id, numeroCuenta, tipoCuenta, etc. |
| IT-2.2 | `POST` | `/cuentas` | Body con clienteId inexistente | `404` + "Cliente no encontrado" |
| IT-2.3 | `POST` | `/cuentas` | Body con numeroCuenta duplicado | `409` + "Ya existe una cuenta con número" |
| IT-2.4 | `POST` | `/cuentas` | Body con tipoCuenta inválido ("Inversión") | `400` + mensaje de validación |
| IT-2.5 | `POST` | `/cuentas` | Body sin numeroCuenta | `400` + mensaje "El número de cuenta es obligatorio" |
| IT-2.6 | `GET` | `/cuentas` | Sin parámetros | `200` + array JSON de cuentas |
| IT-2.7 | `GET` | `/cuentas/{id}` | ID existente | `200` + JSON de la cuenta |
| IT-2.8 | `GET` | `/cuentas/{id}` | ID inexistente | `404` + "Cuenta no encontrada" |
| IT-2.9 | `PUT` | `/cuentas/{id}` | Body completo válido | `200` + JSON con datos actualizados |
| IT-2.10 | `PUT` | `/cuentas/{id}` | ID inexistente | `404` + "Cuenta no encontrada" |
| IT-2.11 | `PATCH` | `/cuentas/{id}` | Solo `{"estado": false}` | `200` + JSON con estado=false |
| IT-2.12 | `PATCH` | `/cuentas/{id}` | ID inexistente | `404` + "Cuenta no encontrada" |
| IT-2.13 | `DELETE` | `/cuentas/{id}` | ID existente | `204` sin body |
| IT-2.14 | `DELETE` | `/cuentas/{id}` | ID inexistente | `404` + "Cuenta no encontrada" |

---

## 4. TAREAS DE IMPLEMENTACIÓN

### Backend
- [ ] Crear entidad `Cliente` (copia local)
- [ ] Crear entidad `Cuenta`
- [ ] Implementar `CuentaCreateDTO`, `CuentaUpdateDTO`, `CuentaPatchDTO`, `CuentaResponseDTO`
- [ ] Implementar `ClienteRepository` (JPA)
- [ ] Implementar `CuentaRepository` (JPA)
- [ ] Implementar `ClienteEventConsumer` (RabbitMQ Consumer)
- [ ] Implementar `CuentaService` — lógica CRUD + validaciones
- [ ] Implementar `CuentaController` con GET/POST/PUT/PATCH/DELETE
- [ ] Configurar RabbitMQ (cola `cliente.queue`, exchange `banking.exchange`)

### Pruebas Unitarias
- [ ] `CuentaServiceTest` — 9 tests (UT-2.1 a UT-2.9)
- [ ] `CuentaRepositoryTest` — 2 tests (UT-2.10, UT-2.11)
- [ ] `ClienteEventConsumerTest` — 1 test (UT-2.12)

### Pruebas de Integración
- [ ] `CuentaControllerIntegrationTest` — 14 tests (IT-2.1 a IT-2.14)

### QA
- [ ] Ejecutar `/gherkin-case-generator` para HU-02
- [ ] Ejecutar `/risk-identifier` para HU-02
