package com.sofka.ms_clientes_personas.service;

import com.sofka.ms_clientes_personas.dto.ClienteCreateDTO;
import com.sofka.ms_clientes_personas.dto.ClientePatchDTO;
import com.sofka.ms_clientes_personas.dto.ClienteResponseDTO;
import com.sofka.ms_clientes_personas.dto.ClienteUpdateDTO;
import java.util.List;

public interface ClienteService {

    ClienteResponseDTO create(ClienteCreateDTO dto);

    List<ClienteResponseDTO> findAll();

    ClienteResponseDTO findById(String clienteId);

    ClienteResponseDTO update(String clienteId, ClienteUpdateDTO dto);

    ClienteResponseDTO patch(String clienteId, ClientePatchDTO dto);

    void delete(String clienteId);
}
