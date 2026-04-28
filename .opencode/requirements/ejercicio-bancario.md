# Requerimiento: Sistema Bancario de Microservicios

> **Perfil:** SemiSenior  
> **Stack:** Java Spring Boot + JPA/Hibernate + PostgreSQL + RabbitMQ  
> **Microservicios:** `ms-clientes-personas` (Cliente/Persona) + `ms-cuentas-movimientos` (Cuenta/Movimientos)  
> **Comunicación:** Async vía RabbitMQ entre microservicios  

---

## 1. Alcance Funcional (F1 - F6)

### F1 — CRUDs para las entidades Cliente, Cuenta y Movimiento
- Crear, editar, actualizar y eliminar registros para todas las entidades.
- Verbos HTTP: GET, POST, PUT, **PATCH**, DELETE.
- Endpoints: `/clientes`, `/cuentas`, `/movimientos`.

### F2 — Registro de Movimientos
- Soportar valores positivos (depósito) y negativos (retiro).
- Al realizar un movimiento se debe actualizar el saldo disponible de la cuenta.
- Llevar el registro histórico de todas las transacciones.

### F3 — Validación de Saldo
- Al realizar un retiro sin saldo suficiente, alertar con el mensaje **"Saldo no disponible"**.
- Capturar y mostrar el error mediante manejo de excepciones HTTP adecuado (HTTP 422).
- Definir la mejor manera de capturar y mostrar el error.

### F4 — Reportes (Estado de Cuenta)
- Generar un reporte de "Estado de cuenta" especificando un rango de fechas y cliente.
- Contenido: Cuentas asociadas con saldos y detalle de movimientos.
- Endpoint: `/reportes?fechaInicio=<fecha>&fechaFin=<fecha>&clienteId=<id>`.
- Formato de salida: **JSON** (ver sección 4.4 para el formato exacto).

### F5 — Pruebas Unitarias
- Mínimo 1 prueba unitaria para la entidad de dominio Cliente.

### F6 — Pruebas de Integración (Deseable)
- Mínimo 1 prueba de integración.

> **Nota adicional del ejercicio (punto 1):** Se debe realizar como mínimo **dos pruebas unitarias de los endpoints** como requisito general, independiente de F5 y F6.

---

## 2. Entidades del Dominio

### Persona
| Campo | Tipo | Restricción | Descripción |
|-------|------|-------------|-------------|
| `id` | Long (PK) | Auto-generado | Identificador único |
| `nombre` | String | Obligatorio | Nombre completo |
| `genero` | String | Opcional | Género |
| `edad` | Integer | Opcional | Edad |
| `identificacion` | String | Unique, obligatorio | Número de identificación |
| `direccion` | String | Obligatorio | Dirección |
| `telefono` | String | Obligatorio | Teléfono |

### Cliente (hereda de Persona)
| Campo | Tipo | Restricción | Descripción |
|-------|------|-------------|-------------|
| `clienteId` | String (PK) | Obligatorio, generado | Identificador único del cliente |
| `contrasena` | String | Obligatorio | Contraseña |
| `estado` | Boolean | Obligatorio | Activo (true) / Inactivo (false) |

### Cuenta
| Campo | Tipo | Restricción | Descripción |
|-------|------|-------------|-------------|
| `id` | Long (PK) | Auto-generado | Identificador único |
| `numeroCuenta` | String | Unique, obligatorio | Número de cuenta |
| `tipoCuenta` | String | Obligatorio | Ahorro / Corriente |
| `saldoInicial` | BigDecimal | Obligatorio | Saldo inicial |
| `estado` | Boolean | Obligatorio | Activo (true) / Inactivo (false) |
| `clienteId` | String (FK) | Obligatorio | Relación con Cliente |

### Movimiento
| Campo | Tipo | Restricción | Descripción |
|-------|------|-------------|-------------|
| `id` | Long (PK) | Auto-generado | Identificador único |
| `fecha` | LocalDateTime | Auto-generado | Fecha del movimiento |
| `tipoMovimiento` | String | Obligatorio | Depósito / Retiro |
| `valor` | BigDecimal | Obligatorio | Valor del movimiento (+/- ) |
| `saldo` | BigDecimal | Obligatorio | Saldo resultante después del movimiento |
| `cuentaId` | Long (FK) | Obligatorio | Relación con Cuenta |

---

## 3. Arquitectura de Microservicios

```
ms-clientes-personas (puerto 8081)    ms-cuentas-movimientos (puerto 8082)
├── Persona (CRUD)                    ├── Cuenta (CRUD)
├── Cliente (CRUD)                    ├── Movimiento (CRUD + lógica)
└── RabbitMQ Producer                 └── RabbitMQ Consumer
         │                                      ▲
         └──────── Eventos Cliente ─────────────┘
                  (cliente.creado, cliente.actualizado)
```

- `ms-clientes-personas` expone `/clientes` y emite eventos al crear/actualizar clientes.
- `ms-cuentas-movimientos` consume eventos de clientes y expone `/cuentas`, `/movimientos`, `/reportes`.

---

## 4. Casos de Uso (Datos de Prueba)

### 4.1 Creación de Clientes
| Nombres | Dirección | Teléfono | Contraseña | Estado |
|:---|:---|:---|:---|:---|
| Jose Lema | Otavalo sn y principal | 098254785 | 1234 | True |
| Marianela Montalvo | Amazonas y NNUU | 097548965 | 5678 | True |
| Juan Osorio | 13 junio y Equinoccial | 098874587 | 1245 | True |

### 4.2 Creación de Cuentas
| Número Cuenta | Tipo | Saldo Inicial | Estado | Cliente |
|:---|:---|:---|:---|:---|
| 478758 | Ahorro | 2000 | True | Jose Lema |
| 225487 | Corriente | 100 | True | Marianela Montalvo |
| 495878 | Ahorros | 0 | True | Juan Osorio |
| 496825 | Ahorros | 540 | True | Marianela Montalvo |
| 585545 | Corriente | 1000 | True | Jose Lema |

### 4.3 Registro de Movimientos
| Cuenta | Movimiento | Valor | Saldo Resultante |
|:---|:---|:---|:---|
| 478758 | Retiro | -575 | 1425 |
| 225487 | Depósito | +600 | 700 |
| 495878 | Depósito | +150 | 150 |
| 496825 | Retiro | -540 | 0 |

### 4.4 Reporte Esperado (Formato JSON)

**Tabla de ejemplo:**
| Fecha | Cliente | Núm. Cuenta | Tipo | Saldo Inicial | Estado | Movimiento | Saldo Disponible |
|:---|:---|:---|:---|:---|:---|:---|:---|
| 10/2/2022 | Marianela Montalvo | 225487 | Corriente | 100 | True | 600 | 700 |
| 8/2/2022 | Marianela Montalvo | 496825 | Ahorros | 540 | True | -540 | 0 |

**Formato JSON exacto por cada registro del reporte:**
```json
{
  "Fecha": "10/2/2022",
  "Cliente": "Marianela Montalvo",
  "Numero Cuenta": "225487",
  "Tipo": "Corriente",
  "Saldo Inicial": 100,
  "Estado": true,
  "Movimiento": 600,
  "Saldo Disponible": 700
}
```

---

## 5. Restricciones Técnicas

- **Patrón Repository** obligatorio para acceso a datos.
- **Manejo de excepciones** con mensajes claros (ej: "Saldo no disponible").
- **Mínimo 2 pruebas unitarias de endpoints** como requisito general.
- **Base de datos relacional** PostgreSQL.
- **JPA / Hibernate** para el mapeo objeto-relacional.
- **Comunicación asíncrona** vía RabbitMQ entre microservicios.
- **Despliegue** en Docker (docker-compose con todos los servicios).
- **Build tool:** Gradle.
- **Paquete base:** `com.sofka.banking`.
- **Validación:** Postman (archivo JSON de colección Postman como entregable).
- **Script DB:** `BaseDatos.sql` con esquema y datos de prueba.

---

## 6. Entregables

1. Repositorio Git público.
2. Archivo JSON de Postman para validación de endpoints.
3. Script `BaseDatos.sql` con esquema y datos.
4. Docker Compose con todos los servicios.

---

## 7. Prioridad

**Alta** — Ejercicio técnico completo para perfil SemiSenior.
