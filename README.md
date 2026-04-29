# Sistema Bancario de Microservicios

Prueba técnica — Arquitectura Microservicio (2023)

**Stack:** Java 21 + Spring Boot 4.0.6 + JPA/Hibernate + PostgreSQL + RabbitMQ + Gradle

---

## Arquitectura

```
┌──────────────────────────┐       RabbitMQ        ┌──────────────────────────┐
│  ms-clientes-personas    │ ◄──────────────────►  │  ms-cuentas-movimientos   │
│  :8081 /clientes         │  cliente.creado        │  :8082 /cuentas           │
│                          │  cliente.actualizado   │         /movimientos      │
│  Persona ← Cliente       │                       │         /reportes         │
│  PostgreSQL :5432        │                       │  Cuenta, Movimiento       │
└──────────────────────────┘                       │  PostgreSQL :5433          │
                                                   └──────────────────────────┘
```

---

## Requisitos

- Java 21
- Docker + Docker Compose
- Gradle (incluido vía wrapper)

---

## Inicio rápido (Docker Compose)

```bash
# Compilar JARs
cd ms-clientes-personas && ./gradlew bootJar && cd ..
cd ms-cuentas-movimientos && ./gradlew bootJar && cd ..

# Levantar todo
docker compose up -d --build
```

### Servicios

| Servicio | URL | Descripción |
|----------|-----|-------------|
| ms-clientes-personas | http://localhost:8081 | CRUD Clientes |
| ms-cuentas-movimientos | http://localhost:8082 | CRUD Cuentas + Movimientos + Reportes |
| RabbitMQ UI | http://localhost:15672 | guest/guest |
| pgAdmin | http://localhost:5050 | admin@sofka.com/admin |

### Swagger UI

| Microservicio | Swagger |
|--------------|---------|
| ms-clientes-personas | http://localhost:8081/swagger-ui.html |
| ms-cuentas-movimientos | http://localhost:8082/swagger-ui.html |

---

## Endpoints

### ms-clientes-personas (`/clientes`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /clientes | Crear cliente |
| GET | /clientes | Listar todos |
| GET | /clientes/{clienteId} | Obtener por ID |
| PUT | /clientes/{clienteId} | Actualizar completo |
| PATCH | /clientes/{clienteId} | Actualizar parcial |
| DELETE | /clientes/{clienteId} | Eliminar |

### ms-cuentas-movimientos

#### `/cuentas`

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /cuentas | Crear cuenta |
| GET | /cuentas | Listar todas |
| GET | /cuentas/{id} | Obtener por ID |
| PUT | /cuentas/{id} | Actualizar completo |
| PATCH | /cuentas/{id} | Actualizar parcial |
| DELETE | /cuentas/{id} | Eliminar |

#### `/movimientos`

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /movimientos | Registrar movimiento |
| GET | /movimientos | Listar todos |
| GET | /movimientos/{id} | Obtener por ID |

#### `/reportes`

| Método | Ruta | Query Params |
|--------|------|-------------|
| GET | /reportes | fechaInicio, fechaFin, clienteId |

---

## Desarrollo local

```bash
# Infraestructura
docker compose up -d postgres-clientes postgres-cuentas rabbitmq pgadmin

# ms-clientes-personas (terminal 1)
cd ms-clientes-personas && ./gradlew bootRun

# ms-cuentas-movimientos (terminal 2)
cd ms-cuentas-movimientos && ./gradlew bootRun
```

---

## Pruebas

```bash
# Ejecutar tests
cd ms-clientes-personas && ./gradlew test && cd ..
cd ms-cuentas-movimientos && ./gradlew test && cd ..
```

| Microservicio | Tests |
|--------------|-------|
| ms-clientes-personas | 28 |
| ms-cuentas-movimientos | 71 |
| **Total** | **99** |

---

## Patrones de diseño

| Patrón | Ubicación | Propósito |
|--------|-----------|-----------|
| **Strategy** | `strategy/DepositoStrategy`, `RetiroStrategy` | Tipos de movimiento extensibles (OCP) |
| **Repository** | `repository/*` | Abstracción de acceso a datos |
| **Mapper** | `mapper/*` | Separación Entity ↔ DTO (SRP) |
| **Template Method** | `BaseEntity.onCreate/onUpdate` | Timestamps automáticos |
| **Dependency Injection** | `@RequiredArgsConstructor` | Inyección por constructor |
| **Global Exception Handler** | `@ControllerAdvice` | Manejo centralizado de errores |

---

## Comunicación asíncrona

Al crear/actualizar un cliente en `ms-clientes-personas`, se publica un evento JSON a RabbitMQ:

```
banking.exchange (topic)
├── cliente.creado    → cliente.queue  → ms-cuentas-movimientos (replica cliente)
└── cliente.actualizado → cliente.queue → ms-cuentas-movimientos (actualiza cliente)
```

---

## Entregables

- [x] Repositorio Git público
- [x] 2 microservicios Spring Boot
- [x] Comunicación asíncrona (RabbitMQ)
- [x] CRUD endpoints (F1-F4)
- [x] Validación de saldo (F3)
- [x] Reportes (F4)
- [x] Pruebas unitarias + integración (F5, F6)
- [x] Docker Compose (F7)
- [x] BaseDatos.sql
- [x] Colección Postman
- [x] Swagger UI
- [x] Patrones SOLID + Strategy
