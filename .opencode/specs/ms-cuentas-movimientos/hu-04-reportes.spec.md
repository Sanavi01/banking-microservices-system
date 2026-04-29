---
id: SPEC-004
status: APPROVED
feature: hu-04-reportes
created: 2026-04-28
updated: 2026-04-28
author: spec-generator
version: "1.0"
related-specs:
  - SPEC-002 (hu-02-crud-cuentas)
  - SPEC-003 (hu-03-movimientos)
microservicio: ms-cuentas-movimientos
---

# Spec HU-04: Reporte de Estado de Cuenta

> **Microservicio:** `ms-cuentas-movimientos` (puerto 8082)
> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de implementar.
> **Endpoint:** `/reportes`

---

## 1. REQUERIMIENTOS

### Historia de Usuario

```
Como:        Cajero del sistema bancario
Quiero:      Generar un reporte de estado de cuenta por rango de fechas y cliente
Para:        Consultar el historial de movimientos, cuentas asociadas y saldos de un cliente

Prioridad:   Alta
Estimación:  M
Dependencias: SPEC-002 (cuentas), SPEC-003 (movimientos) — Requiere datos de cuentas y movimientos registrados
Capa:        Backend (ms-cuentas-movimientos)
```

### Criterios de Aceptación

**Happy Path**
```gherkin
CRITERIO-4.1: Generar reporte con rango de fechas y cliente válidos
  Dado que:  el cliente "Marianela Montalvo" tiene 2 cuentas (225487, 496825) con movimientos en febrero 2022
  Cuando:    envío un GET a /reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022&clienteId={clienteId}
  Entonces:  el sistema retorna 200 con un array JSON de movimientos
  Y         cada registro contiene: Fecha, Cliente, Numero Cuenta, Tipo, Saldo Inicial, Estado, Movimiento, Saldo Disponible

CRITERIO-4.2: Reporte de cliente sin movimientos en el rango
  Dado que:  el cliente "Juan Osorio" tiene cuenta 495878 pero no tiene movimientos en el rango consultado
  Cuando:    envío un GET a /reportes con rango sin movimientos
  Entonces:  el sistema retorna 200 con array vacío []
```

**Error Path**
```gherkin
CRITERIO-4.3: Reporte con cliente inexistente
  Dado que:  no existe un cliente con clienteId "XYZ-999"
  Cuando:    envío un GET a /reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022&clienteId=XYZ-999
  Entonces:  el sistema retorna 404 con mensaje "Cliente no encontrado: XYZ-999"

CRITERIO-4.4: Reporte con parámetros faltantes
  Dado que:  el endpoint requiere fechaInicio, fechaFin y clienteId
  Cuando:    envío un GET a /reportes sin alguno de los parámetros
  Entonces:  el sistema retorna 400 con mensaje indicando el parámetro faltante

CRITERIO-4.5: Reporte con fechaFin menor a fechaInicio
  Dado que:  se envían fechas en orden incorrecto
  Cuando:    envío un GET a /reportes?fechaInicio=28/02/2022&fechaFin=01/02/2022
  Entonces:  el sistema retorna 400 con mensaje "fechaInicio no puede ser mayor a fechaFin"
```

### Reglas de Negocio

1. El reporte filtra movimientos por `fechaInicio` y `fechaFin` (rango inclusivo).
2. El reporte se filtra por `clienteId`.
3. El formato de fechas en los query params es `dd/MM/yyyy`.
4. `Saldo Inicial` es el `saldoInicial` de la cuenta registrado en la BD.
5. `Movimiento` es el `valor` del movimiento (+ para depósito, - para retiro).
6. `Saldo Disponible` es el `saldo` resultante almacenado en el movimiento (saldo después de la transacción).
7. El resultado se ordena por fecha del movimiento de forma ascendente.
8. Cada registro del reporte usa EXACTAMENTE estos nombres de campo en el JSON de respuesta (según el ejercicio):
   - `"Fecha"` (String, formato dd/MM/yyyy)
   - `"Cliente"` (String, nombre del cliente)
   - `"Numero Cuenta"` (String, número de cuenta — nótese el espacio)
   - `"Tipo"` (String, tipo de cuenta)
   - `"Saldo Inicial"` (Number, saldo inicial)
   - `"Estado"` (Boolean)
   - `"Movimiento"` (Number, valor del movimiento)
   - `"Saldo Disponible"` (Number, saldo resultante)

---

## 2. DISEÑO

### DTOs

#### ReporteRequest (Query Params)

```
GET /reportes?fechaInicio=dd/MM/yyyy&fechaFin=dd/MM/yyyy&clienteId={id}
```

```java
@Data
public class ReporteRequest {
    @NotNull @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate fechaInicio;

    @NotNull @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate fechaFin;

    @NotBlank
    private String clienteId;
}
```

#### ReporteResponseDTO (Response)

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteResponseDTO {
    @JsonProperty("Fecha")
    private String fecha;

    @JsonProperty("Cliente")
    private String cliente;

    @JsonProperty("Numero Cuenta")
    private String numeroCuenta;

    @JsonProperty("Tipo")
    private String tipo;

    @JsonProperty("Saldo Inicial")
    private BigDecimal saldoInicial;

    @JsonProperty("Estado")
    private Boolean estado;

    @JsonProperty("Movimiento")
    private BigDecimal movimiento;

    @JsonProperty("Saldo Disponible")
    private BigDecimal saldoDisponible;
}
```

### API Endpoint

| Método | Ruta | Descripción | Parámetros | Response |
|--------|------|-------------|-----------|----------|
| `GET` | `/reportes` | Reporte estado de cuenta | `fechaInicio`, `fechaFin`, `clienteId` (todos requeridos) | `200` + `List<ReporteResponseDTO>` |

**Códigos de error:**
| Código | Caso | Mensaje |
|--------|------|---------|
| `400` | Parámetros faltantes o inválidos | Mensaje descriptivo del error |
| `404` | Cliente no encontrado | "Cliente no encontrado: {clienteId}" |

### Ejemplo de Request/Response

**GET /reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022&clienteId=a1b2c3d4-e5f6-7890-abcd-ef1234567890**

**Response 200:**
```json
[
  {
    "Fecha": "08/02/2022",
    "Cliente": "Marianela Montalvo",
    "Numero Cuenta": "496825",
    "Tipo": "Ahorros",
    "Saldo Inicial": 540,
    "Estado": true,
    "Movimiento": -540,
    "Saldo Disponible": 0
  },
  {
    "Fecha": "10/02/2022",
    "Cliente": "Marianela Montalvo",
    "Numero Cuenta": "225487",
    "Tipo": "Corriente",
    "Saldo Inicial": 100,
    "Estado": true,
    "Movimiento": 600,
    "Saldo Disponible": 700
  }
]
```

### Lógica de Negocio (ReporteService)

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;

    public List<ReporteResponseDTO> generarReporte(ReporteRequest request) {
        // 1. Validar fechas
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new InvalidDateRangeException("fechaInicio no puede ser mayor a fechaFin");
        }

        // 2. Verificar que el cliente existe
        Cliente cliente = clienteRepository.findById(request.getClienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getClienteId()));

        // 3. Convertir fechas a LocalDateTime para el rango
        LocalDateTime inicio = request.getFechaInicio().atStartOfDay();
        LocalDateTime fin = request.getFechaFin().atTime(23, 59, 59);

        // 4. Buscar movimientos de cuentas del cliente en el rango
        List<Movimiento> movimientos = movimientoRepository
            .findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc(
                request.getClienteId(), inicio, fin);

        // 5. Mapear a DTO de reporte
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return movimientos.stream()
            .map(mov -> new ReporteResponseDTO(
                mov.getFecha().format(formatter),           // Fecha
                cliente.getNombre(),                         // Cliente
                mov.getCuenta().getNumeroCuenta(),           // Numero Cuenta
                mov.getCuenta().getTipoCuenta(),             // Tipo
                mov.getCuenta().getSaldoInicial(),           // Saldo Inicial
                mov.getCuenta().getEstado(),                 // Estado
                mov.getValor(),                              // Movimiento
                mov.getSaldo()                               // Saldo Disponible
            ))
            .toList();
    }
}
```

### Repositorio — Query personalizada

```java
@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query("SELECT m FROM Movimiento m " +
           "JOIN FETCH m.cuenta c " +
           "WHERE c.clienteId = :clienteId " +
           "AND m.fecha BETWEEN :inicio AND :fin " +
           "ORDER BY m.fecha ASC")
    List<Movimiento> findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc(
        @Param("clienteId") String clienteId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM Movimiento m WHERE m.cuenta.id = :cuentaId")
    Optional<BigDecimal> sumValorByCuentaId(@Param("cuentaId") Long cuentaId);
}
```

### Capas de Implementación

```
src/main/java/com/sofka/banking/cuentas/
├── dto/
│   ├── ReporteRequest.java         (nuevo — query params)
│   └── ReporteResponseDTO.java     (nuevo — respuesta con @JsonProperty)
├── service/
│   └── ReporteService.java         (nuevo)
├── controller/
│   └── ReporteController.java      (nuevo)
└── exception/
    └── InvalidDateRangeException.java  (nuevo, HTTP 400)
```

---

## 3. PLAN DE PRUEBAS

### 3A. Pruebas Unitarias (Código)

| # | Clase | Método | Descripción |
|---|-------|--------|-------------|
| UT-4.1 | `ReporteServiceTest` | `generarReporte_shouldReturnList_whenClienteYFechasValidos()` | Reporte con datos válidos retorna lista de DTOs |
| UT-4.2 | `ReporteServiceTest` | `generarReporte_shouldReturnEmptyList_whenNoMovimientosInRange()` | Sin movimientos en el rango retorna lista vacía |
| UT-4.3 | `ReporteServiceTest` | `generarReporte_shouldThrow_whenClienteNotFound()` | Cliente inexistente lanza ResourceNotFoundException |
| UT-4.4 | `ReporteServiceTest` | `generarReporte_shouldThrow_whenFechaInicioMayorFechaFin()` | Rango de fechas inválido lanza InvalidDateRangeException |
| UT-4.5 | `ReporteServiceTest` | `generarReporte_shouldMapFieldsCorrectly_toDTO()` | Campos del DTO coinciden con los del JSON esperado (incluyendo @JsonProperty) |
| UT-4.6 | `ReporteServiceTest` | `generarReporte_shouldOrderByFechaAsc()` | Resultados ordenados por fecha ascendente |
| UT-4.7 | `ReporteServiceTest` | `generarReporte_shouldIncludeEdgeDates()` | Movimientos en fechaInicio y fechaFin se incluyen (rango inclusivo) |
| UT-4.8 | `ReporteServiceTest` | `generarReporte_shouldFilterByClienteId_onlyThatCliente()` | Solo retorna movimientos del cliente especificado |
| UT-4.9 | `MovimientoRepositoryTest` | `findByCuentaClienteIdAndFechaBetween_shouldReturnFiltered()` | Query JPA filtra correctamente por clienteId y rango de fechas |

### 3B. Pruebas de Integración (Endpoints)

| # | Método | Endpoint | Escenario | Esperado |
|---|--------|----------|-----------|----------|
| IT-4.1 | `GET` | `/reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022&clienteId={id}` | Cliente Marianela Montalvo con 2 movimientos | `200` + array con 2 registros, campos JSON en español |
| IT-4.2 | `GET` | `/reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022&clienteId={id}` | Validar formato exacto del JSON: "Fecha", "Cliente", "Numero Cuenta", "Tipo", "Saldo Inicial", "Estado", "Movimiento", "Saldo Disponible" | Campos con nombres y tipos correctos |
| IT-4.3 | `GET` | `/reportes?fechaInicio=01/01/2022&fechaFin=01/01/2022&clienteId={id}` | Rango sin movimientos | `200` + array vacío `[]` |
| IT-4.4 | `GET` | `/reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022&clienteId=XYZ-999` | Cliente inexistente | `404` + "Cliente no encontrado" |
| IT-4.5 | `GET` | `/reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022` | Falta parámetro clienteId | `400` + mensaje de validación |
| IT-4.6 | `GET` | `/reportes?fechaFin=28/02/2022&clienteId={id}` | Falta parámetro fechaInicio | `400` + mensaje de validación |
| IT-4.7 | `GET` | `/reportes?fechaInicio=28/02/2022&fechaFin=01/02/2022&clienteId={id}` | fechaInicio > fechaFin | `400` + "fechaInicio no puede ser mayor a fechaFin" |
| IT-4.8 | `GET` | `/reportes?fechaInicio=10/02/2022&fechaFin=10/02/2022&clienteId={id}` | Mismo día para ambas fechas | `200` + movimientos exactamente del 10/02/2022 |
| IT-4.9 | `GET` | `/reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022&clienteId={id}` | Verificar orden ascendente por fecha | Primer elemento con fecha ≤ fecha del último elemento |

### 3C. Validación contra Datos del Ejercicio

Datos del reporte esperado (sección 4.4 del ejercicio):

| Fecha | Cliente | Numero Cuenta | Tipo | Saldo Inicial | Estado | Movimiento | Saldo Disponible |
|:---|:---|:---|:---|:---|:---|:---|:---|
| 08/02/2022 | Marianela Montalvo | 496825 | Ahorros | 540 | true | -540 | 0 |
| 10/02/2022 | Marianela Montalvo | 225487 | Corriente | 100 | true | 600 | 700 |

Test de validación: **IT-4.1** debe producir EXACTAMENTE este JSON (ver sección 2D para el formato exacto).

---

## 4. TAREAS DE IMPLEMENTACIÓN

### Backend
- [ ] Implementar `ReporteRequest` (query params con validación)
- [ ] Implementar `ReporteResponseDTO` con `@JsonProperty` para nombres en español
- [ ] Implementar query JPQL en `MovimientoRepository`: `findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc`
- [ ] Implementar `ReporteService` — lógica de filtro, validación de fechas, mapeo a DTO
- [ ] Implementar `ReporteController` con GET `/reportes`
- [ ] Implementar `InvalidDateRangeException` (HTTP 400)
- [ ] Extender `GlobalExceptionHandler`

### Pruebas Unitarias
- [ ] `ReporteServiceTest` — 8 tests (UT-4.1 a UT-4.8)
- [ ] `MovimientoRepositoryTest` — 1 test de query (UT-4.9)

### Pruebas de Integración
- [ ] `ReporteControllerIntegrationTest` — 9 tests (IT-4.1 a IT-4.9)

### QA
- [ ] Ejecutar `/gherkin-case-generator` para HU-04
- [ ] Ejecutar `/risk-identifier` para HU-04
