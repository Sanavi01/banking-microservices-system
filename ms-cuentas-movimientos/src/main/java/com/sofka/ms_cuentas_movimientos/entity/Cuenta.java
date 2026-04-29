package com.sofka.ms_cuentas_movimientos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cuentas")
public class Cuenta extends BaseEntity {

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
