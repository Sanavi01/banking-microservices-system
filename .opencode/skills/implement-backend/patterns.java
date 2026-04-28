/*
 * patterns.java — Patrones de referencia para el agente backend-spring-boot.
 * Este archivo NO se compila directamente. Es una referencia para que el agente
 * genere código consistente con la arquitectura del proyecto.
 * 
 * Stack: Java 17+ / Spring Boot 3.x / JPA + Hibernate / PostgreSQL / RabbitMQ
 * Paquete base: com.sofka.banking
 */

// ─── ENTITY PATTERN ─────────────────────────────────────────────────────────
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "features")
@Data
@NoArgsConstructor
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// ─── DTO PATTERN ────────────────────────────────────────────────────────────
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeatureCreateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 1, max = 100)
    private String name;

    @Size(max = 500)
    private String description;
}

@Data
public class FeatureResponseDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}

// ─── REPOSITORY PATTERN ─────────────────────────────────────────────────────
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Long> {
    // Spring Data JPA genera los métodos CRUD automáticamente
    Optional<Feature> findByName(String name);
    boolean existsByName(String name);
}

// ─── SERVICE PATTERN ────────────────────────────────────────────────────────
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FeatureService {

    private final FeatureRepository repository;

    @Transactional(readOnly = true)
    public List<FeatureResponseDTO> findAll() {
        return repository.findAll().stream()
            .map(this::toDTO)
            .toList();
    }

    public FeatureResponseDTO create(FeatureCreateDTO dto) {
        Feature entity = new Feature();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        Feature saved = repository.save(entity);
        return toDTO(saved);
    }

    public FeatureResponseDTO update(Long id, FeatureCreateDTO dto) {
        Feature entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Feature no encontrada: " + id));
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return toDTO(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Feature no encontrada: " + id);
        }
        repository.deleteById(id);
    }

    private FeatureResponseDTO toDTO(Feature entity) {
        FeatureResponseDTO dto = new FeatureResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

// ─── CONTROLLER PATTERN ─────────────────────────────────────────────────────
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService service;

    @GetMapping
    public ResponseEntity<List<FeatureResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<FeatureResponseDTO> create(@Valid @RequestBody FeatureCreateDTO dto) {
        FeatureResponseDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FeatureCreateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

// ─── EXCEPTION HANDLER PATTERN ──────────────────────────────────────────────
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.value()));
    }
}

@Data
@AllArgsConstructor
class ErrorResponse {
    private String message;
    private int status;
}
