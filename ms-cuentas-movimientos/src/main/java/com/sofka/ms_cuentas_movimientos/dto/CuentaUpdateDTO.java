package com.sofka.ms_cuentas_movimientos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

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
