package com.sofka.ms_cuentas_movimientos.service;

import com.sofka.ms_cuentas_movimientos.dto.*;
import java.util.List;

public interface CuentaService {

    CuentaResponseDTO create(CuentaCreateDTO dto);

    List<CuentaResponseDTO> findAll();

    CuentaResponseDTO findById(Long id);

    CuentaResponseDTO update(Long id, CuentaUpdateDTO dto);

    CuentaResponseDTO patch(Long id, CuentaPatchDTO dto);

    void delete(Long id);
}
