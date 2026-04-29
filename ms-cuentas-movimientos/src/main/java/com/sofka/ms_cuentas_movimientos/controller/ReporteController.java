package com.sofka.ms_cuentas_movimientos.controller;

import com.sofka.ms_cuentas_movimientos.dto.ReporteRequest;
import com.sofka.ms_cuentas_movimientos.dto.ReporteResponseDTO;
import com.sofka.ms_cuentas_movimientos.service.ReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    public ResponseEntity<List<ReporteResponseDTO>> generarReporte(@Valid ReporteRequest request) {
        return ResponseEntity.ok(reporteService.generarReporte(request));
    }
}
