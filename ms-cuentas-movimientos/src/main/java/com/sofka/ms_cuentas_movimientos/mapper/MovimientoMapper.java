package com.sofka.ms_cuentas_movimientos.mapper;

import com.sofka.ms_cuentas_movimientos.dto.MovimientoResponseDTO;
import com.sofka.ms_cuentas_movimientos.entity.Movimiento;
import org.springframework.stereotype.Component;

@Component
public class MovimientoMapper {

    public MovimientoResponseDTO toResponseDTO(Movimiento movimiento) {
        return MovimientoResponseDTO.builder()
                .id(movimiento.getId())
                .fecha(movimiento.getFecha())
                .tipoMovimiento(movimiento.getTipoMovimiento())
                .valor(movimiento.getValor())
                .saldo(movimiento.getSaldo())
                .cuentaId(movimiento.getCuenta().getId())
                .build();
    }
}
