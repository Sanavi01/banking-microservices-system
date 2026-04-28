package com.sofka.ms_clientes_personas.service.impl;

import com.sofka.ms_clientes_personas.dto.ClienteCreateDTO;
import com.sofka.ms_clientes_personas.dto.ClientePatchDTO;
import com.sofka.ms_clientes_personas.dto.ClienteResponseDTO;
import com.sofka.ms_clientes_personas.dto.ClienteUpdateDTO;
import com.sofka.ms_clientes_personas.entity.Cliente;
import com.sofka.ms_clientes_personas.exception.DuplicateResourceException;
import com.sofka.ms_clientes_personas.exception.ResourceNotFoundException;
import com.sofka.ms_clientes_personas.mapper.ClienteMapper;
import com.sofka.ms_clientes_personas.messaging.ClienteEventPublisher;
import com.sofka.ms_clientes_personas.repository.ClienteRepository;
import com.sofka.ms_clientes_personas.repository.PersonaRepository;
import com.sofka.ms_clientes_personas.service.ClienteService;
import com.sofka.ms_clientes_personas.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final ClienteMapper mapper;
    private final PasswordService passwordService;
    private final ClienteEventPublisher eventPublisher;

    @Override
    public ClienteResponseDTO create(ClienteCreateDTO dto) {
        if (personaRepository.existsByIdentificacion(dto.getIdentificacion())) {
            throw new DuplicateResourceException(
                "Ya existe una persona con identificaci\u00f3n: " + dto.getIdentificacion());
        }

        Cliente cliente = mapper.toEntity(dto);
        cliente.setContrasena(passwordService.hash(dto.getContrasena()));
        Cliente saved = clienteRepository.save(cliente);

        eventPublisher.publishClienteCreado(saved);
        return mapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> findAll() {
        return clienteRepository.findAll().stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO findById(String clienteId) {
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cliente no encontrado: " + clienteId));
        return mapper.toResponseDTO(cliente);
    }

    @Override
    public ClienteResponseDTO update(String clienteId, ClienteUpdateDTO dto) {
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cliente no encontrado: " + clienteId));

        if (dto.getContrasena() != null) {
            dto.setContrasena(passwordService.hash(dto.getContrasena()));
        }
        mapper.updateEntity(cliente, dto);
        Cliente saved = clienteRepository.save(cliente);

        eventPublisher.publishClienteActualizado(saved);
        return mapper.toResponseDTO(saved);
    }

    @Override
    public ClienteResponseDTO patch(String clienteId, ClientePatchDTO dto) {
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cliente no encontrado: " + clienteId));

        if (dto.getContrasena() != null) {
            dto.setContrasena(passwordService.hash(dto.getContrasena()));
        }
        mapper.patchEntity(cliente, dto);
        Cliente saved = clienteRepository.save(cliente);

        eventPublisher.publishClienteActualizado(saved);
        return mapper.toResponseDTO(saved);
    }

    @Override
    public void delete(String clienteId) {
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cliente no encontrado: " + clienteId));
        clienteRepository.delete(cliente);
    }
}
