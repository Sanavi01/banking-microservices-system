package com.sofka.ms_cuentas_movimientos.service.impl;

import com.sofka.ms_cuentas_movimientos.dto.*;
import com.sofka.ms_cuentas_movimientos.entity.Cuenta;
import com.sofka.ms_cuentas_movimientos.exception.DuplicateResourceException;
import com.sofka.ms_cuentas_movimientos.exception.ResourceNotFoundException;
import com.sofka.ms_cuentas_movimientos.mapper.CuentaMapper;
import com.sofka.ms_cuentas_movimientos.repository.ClienteRepository;
import com.sofka.ms_cuentas_movimientos.repository.CuentaRepository;
import com.sofka.ms_cuentas_movimientos.service.CuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final CuentaMapper mapper;

    @Override
    public CuentaResponseDTO create(CuentaCreateDTO dto) {
        if (!clienteRepository.existsById(dto.getClienteId())) {
            throw new ResourceNotFoundException(
                "Cliente no encontrado: " + dto.getClienteId());
        }

        if (cuentaRepository.existsByNumeroCuenta(dto.getNumeroCuenta())) {
            throw new DuplicateResourceException(
                "Ya existe una cuenta con n\u00famero: " + dto.getNumeroCuenta());
        }

        Cuenta cuenta = mapper.toEntity(dto);
        Cuenta saved = cuentaRepository.save(cuenta);
        return mapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> findAll() {
        return cuentaRepository.findAll().stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponseDTO findById(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cuenta no encontrada: " + id));
        return mapper.toResponseDTO(cuenta);
    }

    @Override
    public CuentaResponseDTO update(Long id, CuentaUpdateDTO dto) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cuenta no encontrada: " + id));

        if (dto.getClienteId() != null && !clienteRepository.existsById(dto.getClienteId())) {
            throw new ResourceNotFoundException(
                "Cliente no encontrado: " + dto.getClienteId());
        }

        mapper.updateEntity(cuenta, dto);
        Cuenta saved = cuentaRepository.save(cuenta);
        return mapper.toResponseDTO(saved);
    }

    @Override
    public CuentaResponseDTO patch(Long id, CuentaPatchDTO dto) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cuenta no encontrada: " + id));

        mapper.patchEntity(cuenta, dto);
        Cuenta saved = cuentaRepository.save(cuenta);
        return mapper.toResponseDTO(saved);
    }

    @Override
    public void delete(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cuenta no encontrada: " + id));
        cuentaRepository.delete(cuenta);
    }
}
