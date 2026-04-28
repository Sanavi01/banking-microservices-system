package com.sofka.ms_cuentas_movimientos.strategy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(2)
public class RetiroStrategy implements TipoMovimientoStrategy {

    @Override
    public boolean aplica(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    public String getTipo() {
        return "Retiro";
    }

    @Override
    public boolean requiereValidacionSaldo() {
        return true;
    }
}
