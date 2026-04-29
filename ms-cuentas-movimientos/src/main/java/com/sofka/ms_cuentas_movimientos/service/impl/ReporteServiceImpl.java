package com.sofka.ms_cuentas_movimientos.service.impl;

import com.sofka.ms_cuentas_movimientos.dto.ReporteRequest;
import com.sofka.ms_cuentas_movimientos.dto.ReporteResponseDTO;
import com.sofka.ms_cuentas_movimientos.entity.Cliente;
import com.sofka.ms_cuentas_movimientos.entity.Movimiento;
import com.sofka.ms_cuentas_movimientos.exception.InvalidDateRangeException;
import com.sofka.ms_cuentas_movimientos.exception.ResourceNotFoundException;
import com.sofka.ms_cuentas_movimientos.repository.ClienteRepository;
import com.sofka.ms_cuentas_movimientos.repository.MovimientoRepository;
import com.sofka.ms_cuentas_movimientos.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final MovimientoRepository movimientoRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public List<ReporteResponseDTO> generarReporte(ReporteRequest request) {
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new InvalidDateRangeException("fechaInicio no puede ser mayor a fechaFin");
        }

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cliente no encontrado: " + request.getClienteId()));

        LocalDateTime inicio = request.getFechaInicio().atStartOfDay();
        LocalDateTime fin = request.getFechaFin().atTime(23, 59, 59);

        List<Movimiento> movimientos = movimientoRepository
                .findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc(
                    request.getClienteId(), inicio, fin);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return movimientos.stream()
                .map(mov -> new ReporteResponseDTO(
                    mov.getFecha().format(formatter),
                    cliente.getNombre(),
                    mov.getCuenta().getNumeroCuenta(),
                    mov.getCuenta().getTipoCuenta(),
                    mov.getCuenta().getSaldoInicial(),
                    mov.getCuenta().getEstado(),
                    mov.getValor(),
                    mov.getSaldo()
                ))
                .toList();
    }
}
