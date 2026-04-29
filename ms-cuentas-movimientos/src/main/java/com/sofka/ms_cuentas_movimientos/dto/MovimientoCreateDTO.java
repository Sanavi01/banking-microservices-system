package com.sofka.ms_cuentas_movimientos.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MovimientoCreateDTO {

    @NotNull(message = "El ID de cuenta es obligatorio")
    private Long cuentaId;

    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "-999999999.99")
    @DecimalMax(value = "999999999.99")
    private BigDecimal valor;
}
