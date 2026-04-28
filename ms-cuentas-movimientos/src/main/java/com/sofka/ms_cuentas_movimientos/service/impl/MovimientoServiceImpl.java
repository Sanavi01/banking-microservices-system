package com.sofka.ms_cuentas_movimientos.service.impl;

import com.sofka.ms_cuentas_movimientos.dto.MovimientoCreateDTO;
import com.sofka.ms_cuentas_movimientos.dto.MovimientoResponseDTO;
import com.sofka.ms_cuentas_movimientos.entity.Cuenta;
import com.sofka.ms_cuentas_movimientos.entity.Movimiento;
import com.sofka.ms_cuentas_movimientos.exception.InsufficientBalanceException;
import com.sofka.ms_cuentas_movimientos.exception.ResourceNotFoundException;
import com.sofka.ms_cuentas_movimientos.mapper.MovimientoMapper;
import com.sofka.ms_cuentas_movimientos.repository.CuentaRepository;
import com.sofka.ms_cuentas_movimientos.repository.MovimientoRepository;
import com.sofka.ms_cuentas_movimientos.service.MovimientoService;
import com.sofka.ms_cuentas_movimientos.strategy.TipoMovimientoResolver;
import com.sofka.ms_cuentas_movimientos.strategy.TipoMovimientoStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoMapper mapper;
    private final TipoMovimientoResolver strategyResolver;

    @Override
    public MovimientoResponseDTO registrar(MovimientoCreateDTO dto) {
        TipoMovimientoStrategy strategy = strategyResolver.resolver(dto.getValor());

        Cuenta cuenta = cuentaRepository.findById(dto.getCuentaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cuenta no encontrada: " + dto.getCuentaId()));

        BigDecimal saldoActual = movimientoRepository
                .sumValorByCuentaId(cuenta.getId())
                .orElse(BigDecimal.ZERO)
                .add(cuenta.getSaldoInicial());

        BigDecimal nuevoSaldo = saldoActual.add(dto.getValor());

        if (strategy.requiereValidacionSaldo() && nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException("Saldo no disponible");
        }

        Movimiento movimiento = new Movimiento();
        movimiento.setCuenta(cuenta);
        movimiento.setValor(dto.getValor());
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setTipoMovimiento(strategy.getTipo());

        Movimiento saved = movimientoRepository.save(movimiento);
        return mapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> findAll() {
        return movimientoRepository.findAll().stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoResponseDTO findById(Long id) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Movimiento no encontrado: " + id));
        return mapper.toResponseDTO(movimiento);
    }
}
