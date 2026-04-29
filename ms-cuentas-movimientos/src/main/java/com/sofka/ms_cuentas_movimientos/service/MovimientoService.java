package com.sofka.ms_cuentas_movimientos.service;

import com.sofka.ms_cuentas_movimientos.dto.MovimientoCreateDTO;
import com.sofka.ms_cuentas_movimientos.dto.MovimientoResponseDTO;
import java.util.List;

public interface MovimientoService {

    MovimientoResponseDTO registrar(MovimientoCreateDTO dto);

    List<MovimientoResponseDTO> findAll();

    MovimientoResponseDTO findById(Long id);
}
