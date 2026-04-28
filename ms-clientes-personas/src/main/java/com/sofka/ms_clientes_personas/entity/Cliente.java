package com.sofka.ms_clientes_personas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "persona_id")
public class Cliente extends Persona {

    @Column(name = "cliente_id", nullable = false, unique = true, length = 36)
    private String clienteId;

    @Column(nullable = false)
    @NotBlank
    private String contrasena;

    @Column(nullable = false)
    @NotNull
    private Boolean estado;

    @PrePersist
    public void generateId() {
        if (clienteId == null) {
            clienteId = UUID.randomUUID().toString();
        }
    }
}
