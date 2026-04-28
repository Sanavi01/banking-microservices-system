package com.sofka.ms_clientes_personas.mapper;

import com.sofka.ms_clientes_personas.dto.ClienteCreateDTO;
import com.sofka.ms_clientes_personas.dto.ClienteResponseDTO;
import com.sofka.ms_clientes_personas.dto.ClienteUpdateDTO;
import com.sofka.ms_clientes_personas.dto.ClientePatchDTO;
import com.sofka.ms_clientes_personas.entity.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteCreateDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setGenero(dto.getGenero());
        cliente.setEdad(dto.getEdad());
        cliente.setIdentificacion(dto.getIdentificacion());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setContrasena(dto.getContrasena());
        cliente.setEstado(dto.getEstado());
        return cliente;
    }

    public void updateEntity(Cliente cliente, ClienteUpdateDTO dto) {
        if (dto.getNombre() != null) cliente.setNombre(dto.getNombre());
        if (dto.getGenero() != null) cliente.setGenero(dto.getGenero());
        if (dto.getEdad() != null) cliente.setEdad(dto.getEdad());
        if (dto.getIdentificacion() != null) cliente.setIdentificacion(dto.getIdentificacion());
        if (dto.getDireccion() != null) cliente.setDireccion(dto.getDireccion());
        if (dto.getTelefono() != null) cliente.setTelefono(dto.getTelefono());
        if (dto.getContrasena() != null) cliente.setContrasena(dto.getContrasena());
        if (dto.getEstado() != null) cliente.setEstado(dto.getEstado());
    }

    public void patchEntity(Cliente cliente, ClientePatchDTO dto) {
        if (dto.getNombre() != null) cliente.setNombre(dto.getNombre());
        if (dto.getGenero() != null) cliente.setGenero(dto.getGenero());
        if (dto.getEdad() != null) cliente.setEdad(dto.getEdad());
        if (dto.getIdentificacion() != null) cliente.setIdentificacion(dto.getIdentificacion());
        if (dto.getDireccion() != null) cliente.setDireccion(dto.getDireccion());
        if (dto.getTelefono() != null) cliente.setTelefono(dto.getTelefono());
        if (dto.getContrasena() != null) cliente.setContrasena(dto.getContrasena());
        if (dto.getEstado() != null) cliente.setEstado(dto.getEstado());
    }

    public ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .clienteId(cliente.getClienteId())
                .nombre(cliente.getNombre())
                .genero(cliente.getGenero())
                .edad(cliente.getEdad())
                .identificacion(cliente.getIdentificacion())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .estado(cliente.getEstado())
                .build();
    }
}
