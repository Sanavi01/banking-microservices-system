package com.sofka.ms_cuentas_movimientos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CuentaCreateDTO {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(max = 20)
    private String numeroCuenta;

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    @Pattern(regexp = "^(Ahorro|Corriente)$", message = "Tipo debe ser Ahorro o Corriente")
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo no puede ser negativo")
    private BigDecimal saldoInicial;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;

    @NotBlank(message = "El clienteId es obligatorio")
    private String clienteId;
}
