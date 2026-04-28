package com.sofka.ms_cuentas_movimientos.mapper;

import com.sofka.ms_cuentas_movimientos.dto.*;
import com.sofka.ms_cuentas_movimientos.entity.Cuenta;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class CuentaMapper {

    public Cuenta toEntity(CuentaCreateDTO dto) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(dto.getNumeroCuenta());
        cuenta.setTipoCuenta(dto.getTipoCuenta());
        cuenta.setSaldoInicial(dto.getSaldoInicial());
        cuenta.setEstado(dto.getEstado());
        cuenta.setClienteId(dto.getClienteId());
        return cuenta;
    }

    public void updateEntity(Cuenta cuenta, CuentaUpdateDTO dto) {
        if (dto.getNumeroCuenta() != null) cuenta.setNumeroCuenta(dto.getNumeroCuenta());
        if (dto.getTipoCuenta() != null) cuenta.setTipoCuenta(dto.getTipoCuenta());
        if (dto.getSaldoInicial() != null) cuenta.setSaldoInicial(dto.getSaldoInicial());
        if (dto.getEstado() != null) cuenta.setEstado(dto.getEstado());
        if (dto.getClienteId() != null) cuenta.setClienteId(dto.getClienteId());
    }

    public void patchEntity(Cuenta cuenta, CuentaPatchDTO dto) {
        if (dto.getTipoCuenta() != null) cuenta.setTipoCuenta(dto.getTipoCuenta());
        if (dto.getSaldoInicial() != null) cuenta.setSaldoInicial(dto.getSaldoInicial());
        if (dto.getEstado() != null) cuenta.setEstado(dto.getEstado());
    }

    public CuentaResponseDTO toResponseDTO(Cuenta cuenta, BigDecimal saldoActual) {
        return CuentaResponseDTO.builder()
                .id(cuenta.getId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoCuenta(cuenta.getTipoCuenta())
                .saldoInicial(cuenta.getSaldoInicial())
                .saldoActual(saldoActual)
                .estado(cuenta.getEstado())
                .clienteId(cuenta.getClienteId())
                .build();
    }
}
