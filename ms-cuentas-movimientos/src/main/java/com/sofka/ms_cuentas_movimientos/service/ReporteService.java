package com.sofka.ms_cuentas_movimientos.service;

import com.sofka.ms_cuentas_movimientos.dto.ReporteRequest;
import com.sofka.ms_cuentas_movimientos.dto.ReporteResponseDTO;
import java.util.List;

public interface ReporteService {

    List<ReporteResponseDTO> generarReporte(ReporteRequest request);
}
