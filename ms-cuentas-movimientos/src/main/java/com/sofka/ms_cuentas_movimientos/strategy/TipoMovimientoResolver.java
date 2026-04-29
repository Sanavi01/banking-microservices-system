package com.sofka.ms_cuentas_movimientos.strategy;

import com.sofka.ms_cuentas_movimientos.exception.InvalidMovementException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TipoMovimientoResolver {

    private final List<TipoMovimientoStrategy> strategies;

    public TipoMovimientoResolver(List<TipoMovimientoStrategy> strategies) {
        this.strategies = strategies;
    }

    public TipoMovimientoStrategy resolver(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidMovementException("El valor del movimiento no puede ser cero");
        }
        return strategies.stream()
                .filter(s -> s.aplica(valor))
                .findFirst()
                .orElseThrow(() -> new InvalidMovementException(
                    "No se encontró estrategia para el valor: " + valor));
    }
}
