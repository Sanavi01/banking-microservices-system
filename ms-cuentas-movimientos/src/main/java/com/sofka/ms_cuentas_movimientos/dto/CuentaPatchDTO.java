package com.sofka.ms_cuentas_movimientos.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CuentaPatchDTO {
    private String tipoCuenta;
    private BigDecimal saldoInicial;
    private Boolean estado;
}
