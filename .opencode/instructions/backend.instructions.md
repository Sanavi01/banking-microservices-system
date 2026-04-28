---
applyTo: "**/*.java"
---

> **Scope**: Se aplica a proyectos Java Spring Boot con JPA/Hibernate + PostgreSQL. Define capas, naming y patrones de DI.

# Instrucciones para Archivos de Backend (Java Spring Boot)

## Arquitectura en Capas

Siempre sigue la arquitectura en capas del proyecto:

```
Controller → Service → Repository → JPA Entity (PostgreSQL)
```

- **`controller/`**: Solo parsear HTTP + delegar al service. Anotar con `@RestController` y `@RequestMapping`.
- **`service/`**: Solo lógica de negocio. Recibe repository por constructor (DI de Spring). Anotar con `@Service` y `@Transactional`.
- **`repository/`**: Interfaces de Spring Data JPA. Extienden `JpaRepository<Entity, ID>`. Anotar con `@Repository`.
- **`model/entity/`**: Entidades JPA con `@Entity`, `@Table`. Contienen `@Id`, `@GeneratedValue`, `@Column`.
- **`model/dto/`**: DTOs para input/output. Sin lógica. Usar `@Data` (Lombok) y `@Valid` para validación.

## Wiring de Dependencias (patrón obligatorio con Spring DI)

```java
// ✅ Correcto — inyección por constructor con Lombok @RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService service;

    @PostMapping
    public ResponseEntity<FeatureResponseDTO> create(@Valid @RequestBody FeatureCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }
}
```

Alternativa: `@Autowired` en el constructor.

NUNCA instanciar servicios o repositorios con `new` — usar siempre DI de Spring.

## Convenciones de Código

- Paquete base: `com.sofka.banking`
- Nombres de clases en `PascalCase` (ej: `ClienteController`, `CuentaService`)
- Nombres de métodos en `camelCase` (ej: `findByClienteId`, `createCliente`)
- Nombres de tablas en `snake_case` y plural (ej: `clientes`, `cuentas`)
- Entidades JPA: `@Entity` + `@Table(name = "...")`
- Timestamps: `created_at`, `updated_at` con `@PrePersist` / `@PreUpdate`
- Validación de entrada con `@Valid`, `@NotBlank`, `@NotNull`, `@Positive`, etc.
- Manejo de excepciones con `@ControllerAdvice` + `@ExceptionHandler`

## Nuevos Controladores

Para agregar un nuevo endpoint:
1. Crear la entidad JPA en `model/entity/`
2. Crear el repositorio en `repository/` (interface que extiende `JpaRepository`)
3. Crear el servicio en `service/` con lógica de negocio
4. Crear el controlador en `controller/` con endpoints REST
5. Los DTOs van en `model/dto/`

> Ver `README.md` para la estructura de carpetas específica del proyecto.

## Manejo de Excepciones

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorResponse(ex.getMessage(), 422));
    }
}
```

## RabbitMQ — Comunicación Asíncrona

- **Producer**: Inyectar `RabbitTemplate`, usar `convertAndSend(exchange, routingKey, message)`.
- **Consumer**: Usar `@RabbitListener(queues = "...")` en un método anotado.
- Exchange tipo `topic` con routing keys: `cliente.creado`, `cliente.actualizado`.
- Configuración en `application.properties` o `application.yml`.

## Nunca hacer

- Lógica de negocio en los controladores (solo en servicios).
- Queries directas en servicios (usar siempre repositorios).
- Exponer entidades JPA directamente en las respuestas (usar DTOs).
- `new Service()` o `new Repository()` (usar DI de Spring siempre).

---

> Para estándares de código limpio, SOLID, nombrado, API REST, seguridad y observabilidad, ver `.opencode/docs/lineamientos/dev-guidelines.md`.
