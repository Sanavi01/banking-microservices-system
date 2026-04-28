package com.sofka.ms_cuentas_movimientos.controller;

import com.sofka.ms_cuentas_movimientos.dto.MovimientoCreateDTO;
import com.sofka.ms_cuentas_movimientos.dto.MovimientoResponseDTO;
import com.sofka.ms_cuentas_movimientos.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping
    public ResponseEntity<MovimientoResponseDTO> registrar(@Valid @RequestBody MovimientoCreateDTO dto) {
        MovimientoResponseDTO created = movimientoService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<MovimientoResponseDTO>> findAll() {
        return ResponseEntity.ok(movimientoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponseDTO> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(movimientoService.findById(id));
    }
}
