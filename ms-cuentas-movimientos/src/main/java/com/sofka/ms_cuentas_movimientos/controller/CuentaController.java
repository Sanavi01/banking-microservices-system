package com.sofka.ms_cuentas_movimientos.controller;

import com.sofka.ms_cuentas_movimientos.dto.*;
import com.sofka.ms_cuentas_movimientos.service.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<CuentaResponseDTO> create(@Valid @RequestBody CuentaCreateDTO dto) {
        CuentaResponseDTO created = cuentaService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponseDTO>> findAll() {
        return ResponseEntity.ok(cuentaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponseDTO> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(cuentaService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponseDTO> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody CuentaUpdateDTO dto) {
        return ResponseEntity.ok(cuentaService.update(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CuentaResponseDTO> patch(
            @PathVariable("id") Long id,
            @RequestBody CuentaPatchDTO dto) {
        return ResponseEntity.ok(cuentaService.patch(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        cuentaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
