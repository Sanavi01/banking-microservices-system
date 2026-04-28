package com.sofka.ms_clientes_personas.controller;

import com.sofka.ms_clientes_personas.dto.ClienteCreateDTO;
import com.sofka.ms_clientes_personas.dto.ClientePatchDTO;
import com.sofka.ms_clientes_personas.dto.ClienteResponseDTO;
import com.sofka.ms_clientes_personas.dto.ClienteUpdateDTO;
import com.sofka.ms_clientes_personas.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> create(@Valid @RequestBody ClienteCreateDTO dto) {
        ClienteResponseDTO created = clienteService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> findAll() {
        return ResponseEntity.ok(clienteService.findAll());
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> findById(@PathVariable("clienteId") String clienteId) {
        return ResponseEntity.ok(clienteService.findById(clienteId));
    }

    @PutMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> update(
            @PathVariable("clienteId") String clienteId,
            @Valid @RequestBody ClienteUpdateDTO dto) {
        return ResponseEntity.ok(clienteService.update(clienteId, dto));
    }

    @PatchMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> patch(
            @PathVariable("clienteId") String clienteId,
            @RequestBody ClientePatchDTO dto) {
        return ResponseEntity.ok(clienteService.patch(clienteId, dto));
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> delete(@PathVariable("clienteId") String clienteId) {
        clienteService.delete(clienteId);
        return ResponseEntity.noContent().build();
    }
}
