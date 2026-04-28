package com.sofka.ms_cuentas_movimientos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "clientes")
public class Cliente extends BaseEntity {

    @Id
    @Column(name = "cliente_id", length = 36)
    private String clienteId;

    @Column(nullable = false, length = 200)
    @NotBlank
    private String nombre;

    @Column(nullable = false)
    @NotNull
    private Boolean estado;
}
