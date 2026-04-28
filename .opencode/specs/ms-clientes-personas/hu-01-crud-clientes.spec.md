---
id: SPEC-001
status: DRAFT
feature: hu-01-crud-clientes
created: 2026-04-28
updated: 2026-04-28
author: spec-generator
version: "1.0"
related-specs: []
microservicio: ms-clientes-personas
---

# Spec HU-01: CRUD de Clientes (Persona + Cliente)

> **Microservicio:** `ms-clientes-personas` (puerto 8081)
> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de implementar.
> **Endpoints:** `/clientes`

---

## 1. REQUERIMIENTOS

### Historia de Usuario

```
Como:        Cajero del sistema bancario
Quiero:      Crear, consultar, editar y eliminar clientes con sus datos personales
Para:        Mantener actualizado el registro de clientes del banco

Prioridad:   Alta
Estimación:  L
Dependencias: Ninguna
Capa:        Backend (ms-clientes-personas)
```

### Criterios de Aceptación

**Happy Path**
```gherkin
CRITERIO-1.1: Crear un cliente con todos los datos válidos
  Dado que:  el sistema está disponible
  Cuando:    envío un POST a /clientes con nombre, género, edad, identificación, dirección, teléfono, contraseña, estado
  Entonces:  el sistema crea el cliente y persona asociada y retorna 201 con los datos del cliente (sin contraseña)

CRITERIO-1.2: Obtener todos los clientes
  Dado que:  existen clientes registrados en el sistema
  Cuando:    envío un GET a /clientes
  Entonces:  el sistema retorna 200 con una lista de todos los clientes

CRITERIO-1.3: Obtener un cliente por ID
  Dado que:  existe un cliente con clienteId "CLI-001"
  Cuando:    envío un GET a /clientes/CLI-001
  Entonces:  el sistema retorna 200 con los datos del cliente

CRITERIO-1.4: Actualizar un cliente (PUT)
  Dado que:  existe un cliente con clienteId "CLI-001"
  Cuando:    envío un PUT a /clientes/CLI-001 con nuevos datos
  Entonces:  el sistema actualiza los datos y retorna 200 con el cliente actualizado

CRITERIO-1.5: Actualizar parcialmente un cliente (PATCH)
  Dado que:  existe un cliente con clienteId "CLI-001" y estado true
  Cuando:    envío un PATCH a /clientes/CLI-001 con {"estado": false}
  Entonces:  el sistema actualiza solo el campo estado y retorna 200

CRITERIO-1.6: Eliminar un cliente (DELETE)
  Dado que:  existe un cliente con clienteId "CLI-001"
  Cuando:    envío un DELETE a /clientes/CLI-001
  Entonces:  el sistema elimina el cliente y retorna 204 sin contenido
```

**Error Path**
```gherkin
CRITERIO-1.7: Crear cliente con datos inválidos
  Dado que:  el sistema está disponible
  Cuando:    envío un POST a /clientes con nombre vacío o campos obligatorios faltantes
  Entonces:  el sistema retorna 400 con mensaje descriptivo del error de validación

CRITERIO-1.8: Obtener cliente inexistente
  Dado que:  no existe un cliente con clienteId "XYZ-999"
  Cuando:    envío un GET a /clientes/XYZ-999
  Entonces:  el sistema retorna 404 con mensaje "Cliente no encontrado"

CRITERIO-1.9: Actualizar cliente inexistente
  Dado que:  no existe un cliente con clienteId "XYZ-999"
  Cuando:    envío un PUT o PATCH a /clientes/XYZ-999
  Entonces:  el sistema retorna 404 con mensaje "Cliente no encontrado"

CRITERIO-1.10: Eliminar cliente inexistente
  Dado que:  no existe un cliente con clienteId "XYZ-999"
  Cuando:    envío un DELETE a /clientes/XYZ-999
  Entonces:  el sistema retorna 404 con mensaje "Cliente no encontrado"
```

### Reglas de Negocio

1. Cliente hereda de Persona (herencia JOINED en JPA).
2. `clienteId` se genera automáticamente (UUID) al crear el cliente.
3. La contraseña se almacena hasheada (BCrypt), nunca se retorna en las respuestas.
4. `identificacion` debe ser única en la tabla personas.
5. `nombre`, `direccion`, `telefono`, `identificacion`, `contrasena`, `estado` son obligatorios.
6. Al crear un cliente exitosamente, se emite evento `cliente.creado` a RabbitMQ.
7. Al actualizar un cliente, se emite evento `cliente.actualizado` a RabbitMQ.
8. La eliminación es física (DELETE hard), no soft delete.

---

## 2. DISEÑO

### Entidades JPA

#### Persona (tabla: personas)

```java
@Entity
@Table(name = "personas")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @NotBlank
    private String nombre;

    @Column(length = 20)
    private String genero;

    private Integer edad;

    @Column(nullable = false, unique = true, length = 20)
    @NotBlank
    private String identificacion;

    @Column(nullable = false, length = 300)
    @NotBlank
    private String direccion;

    @Column(nullable = false, length = 20)
    @NotBlank
    private String telefono;
}
```

#### Cliente (tabla: clientes)

```java
@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "id")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cliente extends Persona {

    @Id
    @Column(name = "cliente_id", length = 36)
    private String clienteId;

    @Column(nullable = false)
    @NotBlank
    private String contrasena;

    @Column(nullable = false)
    private Boolean estado;

    @PrePersist
    public void generateId() {
        if (this.clienteId == null) {
            this.clienteId = UUID.randomUUID().toString();
        }
    }
}
```

### DTOs

#### ClienteCreateDTO (Request — POST)

```java
@Data
public class ClienteCreateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    private String nombre;

    @Size(max = 20)
    private String genero;

    @Min(0) @Max(150)
    private Integer edad;

    @NotBlank(message = "La identificación es obligatoria")
    @Size(max = 20)
    private String identificacion;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 300)
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20)
    private String telefono;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, max = 100)
    private String contrasena;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
```

#### ClienteUpdateDTO (Request — PUT)

```java
@Data
public class ClienteUpdateDTO {
    // Todos los campos del CreateDTO son opcionales en update
    // excepto que los campos enviados se validan igual
    @Size(max = 200)
    private String nombre;
    // ... resto de campos idénticos a CreateDTO pero sin @NotBlank
}
```

#### ClientePatchDTO (Request — PATCH)

```java
@Data
public class ClientePatchDTO {
    // Solo los campos enviados se actualizan — todos opcionales
    private String nombre;
    private String genero;
    private Integer edad;
    private String identificacion;
    private String direccion;
    private String telefono;
    private String contrasena;
    private Boolean estado;
}
```

#### ClienteResponseDTO (Response — GET/POST/PUT/PATCH)

```json
{
  "clienteId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "nombre": "Jose Lema",
  "genero": "Masculino",
  "edad": 30,
  "identificacion": "1234567890",
  "direccion": "Otavalo sn y principal",
  "telefono": "098254785",
  "estado": true
}
```

**Nota:** La contraseña NUNCA se incluye en la respuesta.

### API Endpoints

| Método | Ruta | Descripción | Request Body | Response |
|--------|------|-------------|-------------|----------|
| `POST` | `/clientes` | Crear cliente | `ClienteCreateDTO` | `201` + `ClienteResponseDTO` |
| `GET` | `/clientes` | Listar todos | — | `200` + `List<ClienteResponseDTO>` |
| `GET` | `/clientes/{clienteId}` | Obtener por ID | — | `200` + `ClienteResponseDTO` |
| `PUT` | `/clientes/{clienteId}` | Actualizar completo | `ClienteUpdateDTO` | `200` + `ClienteResponseDTO` |
| `PATCH` | `/clientes/{clienteId}` | Actualizar parcial | `ClientePatchDTO` | `200` + `ClienteResponseDTO` |
| `DELETE` | `/clientes/{clienteId}` | Eliminar | — | `204` No Content |

**Códigos de error comunes:**
| Código | Caso | Mensaje |
|--------|------|---------|
| `400` | Datos inválidos o faltantes | Mensaje de validación del campo específico |
| `404` | Cliente no encontrado | "Cliente no encontrado: {clienteId}" |
| `409` | Identificación duplicada | "Ya existe una persona con identificación: {identificacion}" |
| `500` | Error interno | "Error interno del servidor" |

### Ejemplo de Request/Response

**POST /clientes — Request:**
```json
{
  "nombre": "Jose Lema",
  "genero": "Masculino",
  "edad": 30,
  "identificacion": "1234567890",
  "direccion": "Otavalo sn y principal",
  "telefono": "098254785",
  "contrasena": "1234",
  "estado": true
}
```

**POST /clientes — Response 201:**
```json
{
  "clienteId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "nombre": "Jose Lema",
  "genero": "Masculino",
  "edad": 30,
  "identificacion": "1234567890",
  "direccion": "Otavalo sn y principal",
  "telefono": "098254785",
  "estado": true
}
```

**PATCH /clientes/{clienteId} — Request:**
```json
{
  "estado": false
}
```

**PATCH /clientes/{clienteId} — Response 200:**
```json
{
  "clienteId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "nombre": "Jose Lema",
  "genero": "Masculino",
  "edad": 30,
  "identificacion": "1234567890",
  "direccion": "Otavalo sn y principal",
  "telefono": "098254785",
  "estado": false
}
```

### RabbitMQ Producer

Al crear/actualizar un cliente, se emite un evento:

```java
// Configuración
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
banking.rabbitmq.exchange=banking.exchange
banking.rabbitmq.routing.cliente-creado=cliente.creado
banking.rabbitmq.routing.cliente-actualizado=cliente.actualizado
```

**Payload del evento `cliente.creado`:**
```json
{
  "clienteId": "a1b2c3d4-...",
  "nombre": "Jose Lema",
  "evento": "CLIENTE_CREADO",
  "timestamp": "2026-04-28T10:30:00"
}
```

**Payload del evento `cliente.actualizado`:**
```json
{
  "clienteId": "a1b2c3d4-...",
  "nombre": "Jose Lema Actualizado",
  "estado": false,
  "evento": "CLIENTE_ACTUALIZADO",
  "timestamp": "2026-04-28T11:00:00"
}
```

### Capas de Implementación (ms-clientes-personas)

```
src/main/java/com/sofka/banking/clientes/
├── entity/
│   ├── Persona.java
│   └── Cliente.java
├── dto/
│   ├── ClienteCreateDTO.java
│   ├── ClienteUpdateDTO.java
│   ├── ClientePatchDTO.java
│   └── ClienteResponseDTO.java
├── repository/
│   ├── PersonaRepository.java
│   └── ClienteRepository.java
├── service/
│   └── ClienteService.java
├── controller/
│   └── ClienteController.java
├── messaging/
│   └── ClienteEventPublisher.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
└── config/
    └── RabbitMQConfig.java
```

---

## 3. PLAN DE PRUEBAS

### 3A. Pruebas Unitarias (Código)

| # | Clase de Test | Método de Test | Descripción |
|---|--------------|----------------|-------------|
| UT-1.1 | `ClienteServiceTest` | `create_shouldReturnDto_whenValidData()` | Crear cliente con datos válidos retorna DTO |
| UT-1.2 | `ClienteServiceTest` | `create_shouldHashPassword()` | La contraseña se almacena hasheada con BCrypt |
| UT-1.3 | `ClienteServiceTest` | `create_shouldThrow_whenIdentificacionDuplicada()` | Crear cliente con identificación existente lanza 409 |
| UT-1.4 | `ClienteServiceTest` | `create_shouldPublishEvent_toRabbitMQ()` | Al crear cliente se publica evento `cliente.creado` |
| UT-1.5 | `ClienteServiceTest` | `findById_shouldReturnDto_whenExists()` | Buscar por clienteId existente retorna DTO |
| UT-1.6 | `ClienteServiceTest` | `findById_shouldThrow_whenNotFound()` | Buscar clienteId inexistente lanza ResourceNotFoundException |
| UT-1.7 | `ClienteServiceTest` | `findAll_shouldReturnList()` | Listar todos retorna lista de DTOs |
| UT-1.8 | `ClienteServiceTest` | `update_shouldUpdateAndPublishEvent()` | PUT actualiza todos los campos y emite `cliente.actualizado` |
| UT-1.9 | `ClienteServiceTest` | `patch_shouldUpdateOnlyProvidedFields()` | PATCH actualiza solo los campos enviados |
| UT-1.10 | `ClienteServiceTest` | `delete_shouldRemoveCliente()` | DELETE elimina el cliente y la persona asociada |
| UT-1.11 | `ClienteServiceTest` | `delete_shouldThrow_whenNotFound()` | DELETE con clienteId inexistente lanza excepción |
| UT-1.12 | `ClienteRepositoryTest` | `save_shouldPersistCliente()` | save() persiste cliente y retorna entidad con ID |
| UT-1.13 | `ClienteRepositoryTest` | `findByClienteId_shouldReturnCliente()` | findByClienteId retorna la entidad cuando existe |

### 3B. Pruebas de Integración (Endpoints)

| # | Método HTTP | Endpoint | Escenario | Esperado |
|---|------------|----------|-----------|----------|
| IT-1.1 | `POST` | `/clientes` | Body completo y válido | `201` + JSON con clienteId, nombre, etc. (sin contrasena) |
| IT-1.2 | `POST` | `/clientes` | Body con nombre vacío (`""`) | `400` + mensaje "El nombre es obligatorio" |
| IT-1.3 | `POST` | `/clientes` | Body sin campo `identificacion` | `400` + mensaje "La identificación es obligatoria" |
| IT-1.4 | `POST` | `/clientes` | Body con identificación ya existente | `409` + mensaje de duplicado |
| IT-1.5 | `GET` | `/clientes` | Sin parámetros | `200` + array JSON de clientes |
| IT-1.6 | `GET` | `/clientes/{clienteId}` | clienteId existente | `200` + JSON del cliente |
| IT-1.7 | `GET` | `/clientes/{clienteId}` | clienteId inexistente | `404` + mensaje "Cliente no encontrado" |
| IT-1.8 | `PUT` | `/clientes/{clienteId}` | Body completo válido | `200` + JSON con datos actualizados |
| IT-1.9 | `PUT` | `/clientes/{clienteId}` | clienteId inexistente | `404` + mensaje "Cliente no encontrado" |
| IT-1.10 | `PATCH` | `/clientes/{clienteId}` | Solo `{"nombre": "Nuevo Nombre"}` | `200` + JSON con nombre actualizado, resto igual |
| IT-1.11 | `PATCH` | `/clientes/{clienteId}` | Solo `{"estado": false}` | `200` + JSON con estado=false, resto igual |
| IT-1.12 | `PATCH` | `/clientes/{clienteId}` | clienteId inexistente | `404` + mensaje "Cliente no encontrado" |
| IT-1.13 | `DELETE` | `/clientes/{clienteId}` | clienteId existente | `204` sin body |
| IT-1.14 | `DELETE` | `/clientes/{clienteId}` | clienteId inexistente | `404` + mensaje "Cliente no encontrado" |

### 3C. Cobertura de Reglas de Negocio

| Regla | Cubierta por |
|-------|-------------|
| Cliente hereda de Persona (JOINED) | UT-1.1, UT-1.12 |
| clienteId autogenerado (UUID) | UT-1.1, IT-1.1 |
| Contraseña hasheada, nunca en respuesta | UT-1.2, IT-1.1 |
| identificación única | UT-1.3, IT-1.4 |
| Campos obligatorios validados | IT-1.2, IT-1.3 |
| Evento RabbitMQ al crear | UT-1.4 |
| Evento RabbitMQ al actualizar | UT-1.8 |
| Manejo 404 para operaciones sobre no existentes | IT-1.7, IT-1.9, IT-1.12, IT-1.14 |

### 3D. Datos de Prueba (Seed)

Ejecutar en orden antes de los tests de integración:

```sql
-- Insertar en BaseDatos.sql
INSERT INTO personas (nombre, genero, edad, identificacion, direccion, telefono)
VALUES ('Jose Lema', 'Masculino', 30, '1234567890', 'Otavalo sn y principal', '098254785');
-- El clienteId se genera automáticamente por la aplicación
```

---

## 4. TAREAS DE IMPLEMENTACIÓN

### Backend
- [ ] Crear proyecto `ms-clientes-personas` con Spring Boot + Gradle (puerto 8081)
- [ ] Configurar `application.yml` con PostgreSQL (`db_clientes`) y RabbitMQ
- [ ] Implementar entidad `Persona` con herencia JOINED
- [ ] Implementar entidad `Cliente` (extiende Persona)
- [ ] Implementar `ClienteCreateDTO`, `ClienteUpdateDTO`, `ClientePatchDTO`, `ClienteResponseDTO`
- [ ] Implementar `PersonaRepository` (Spring Data JPA)
- [ ] Implementar `ClienteRepository` (Spring Data JPA)
- [ ] Implementar `ClienteService` — lógica CRUD + hash de contraseña + validaciones
- [ ] Implementar `ClienteController` con endpoints GET/POST/PUT/PATCH/DELETE
- [ ] Implementar `ClienteEventPublisher` (RabbitMQ Producer)
- [ ] Configurar `RabbitMQConfig` (exchange, routing keys)
- [ ] Implementar `GlobalExceptionHandler` con `@ControllerAdvice`
- [ ] Implementar `ResourceNotFoundException` y `DuplicateResourceException`

### Pruebas Unitarias
- [ ] `ClienteServiceTest` — 11 tests (UT-1.1 a UT-1.11)
- [ ] `ClienteRepositoryTest` — 2 tests con `@DataJpaTest` (UT-1.12, UT-1.13)

### Pruebas de Integración
- [ ] `ClienteControllerIntegrationTest` — 14 tests con `@SpringBootTest` + `MockMvc` (IT-1.1 a IT-1.14)

### QA
- [ ] Ejecutar `/gherkin-case-generator` para HU-01
- [ ] Ejecutar `/risk-identifier` para HU-01
