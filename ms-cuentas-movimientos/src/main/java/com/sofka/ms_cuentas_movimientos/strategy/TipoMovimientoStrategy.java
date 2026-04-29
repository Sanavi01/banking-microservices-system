package com.sofka.ms_cuentas_movimientos.strategy;

import java.math.BigDecimal;

public interface TipoMovimientoStrategy {
    boolean aplica(BigDecimal valor);
    String getTipo();
    boolean requiereValidacionSaldo();
}
