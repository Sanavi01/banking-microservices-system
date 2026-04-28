package com.sofka.ms_clientes_personas.service;

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
import com.sofka.ms_clientes_personas.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService Unit Tests")
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private ClienteMapper mapper;
    @Mock private PasswordService passwordService;
    @Mock private ClienteEventPublisher eventPublisher;

    @InjectMocks
    private ClienteServiceImpl service;

    private ClienteCreateDTO createDTO;
    private Cliente cliente;
    private ClienteResponseDTO responseDTO;
    private static final String CLIENTE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    @BeforeEach
    void setUp() {
        createDTO = new ClienteCreateDTO();
        createDTO.setNombre("Jose Lema");
        createDTO.setGenero("Masculino");
        createDTO.setEdad(30);
        createDTO.setIdentificacion("1234567890");
        createDTO.setDireccion("Otavalo sn y principal");
        createDTO.setTelefono("098254785");
        createDTO.setContrasena("1234");
        createDTO.setEstado(true);

        cliente = new Cliente();
        cliente.setClienteId(CLIENTE_ID);
        cliente.setNombre("Jose Lema");
        cliente.setGenero("Masculino");
        cliente.setEdad(30);
        cliente.setIdentificacion("1234567890");
        cliente.setDireccion("Otavalo sn y principal");
        cliente.setTelefono("098254785");
        cliente.setContrasena("hashedPassword");
        cliente.setEstado(true);

        responseDTO = ClienteResponseDTO.builder()
                .clienteId(CLIENTE_ID)
                .nombre("Jose Lema")
                .genero("Masculino")
                .edad(30)
                .identificacion("1234567890")
                .direccion("Otavalo sn y principal")
                .telefono("098254785")
                .estado(true)
                .build();
    }

    @Test
    @DisplayName("UT-1.1: create() con datos válidos retorna DTO")
    void create_shouldReturnDto_whenValidData() {
        given(personaRepository.existsByIdentificacion("1234567890")).willReturn(false);
        given(mapper.toEntity(createDTO)).willReturn(cliente);
        given(passwordService.hash("1234")).willReturn("hashedPassword");
        given(clienteRepository.save(cliente)).willReturn(cliente);
        given(mapper.toResponseDTO(cliente)).willReturn(responseDTO);

        ClienteResponseDTO result = service.create(createDTO);

        assertThat(result.getClienteId()).isEqualTo(CLIENTE_ID);
        assertThat(result.getNombre()).isEqualTo("Jose Lema");
        assertThat(result.getClienteId()).isNotNull();
        then(eventPublisher).should().publishClienteCreado(cliente);
    }

    @Test
    @DisplayName("UT-1.2: create() hashea la contraseña con BCrypt")
    void create_shouldHashPassword() {
        given(personaRepository.existsByIdentificacion("1234567890")).willReturn(false);
        given(mapper.toEntity(createDTO)).willReturn(cliente);
        given(passwordService.hash("1234")).willReturn("hashedPassword");
        given(clienteRepository.save(cliente)).willReturn(cliente);
        given(mapper.toResponseDTO(cliente)).willReturn(responseDTO);

        service.create(createDTO);

        then(passwordService).should().hash("1234");
        assertThat(cliente.getContrasena()).isEqualTo("hashedPassword");
    }

    @Test
    @DisplayName("UT-1.3: create() lanza DuplicateResourceException cuando identificación ya existe")
    void create_shouldThrow_whenIdentificacionDuplicada() {
        given(personaRepository.existsByIdentificacion("1234567890")).willReturn(true);

        assertThatThrownBy(() -> service.create(createDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("1234567890");
        then(clienteRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("UT-1.4: create() publica evento cliente.creado en RabbitMQ")
    void create_shouldPublishEvent_toRabbitMQ() {
        given(personaRepository.existsByIdentificacion("1234567890")).willReturn(false);
        given(mapper.toEntity(createDTO)).willReturn(cliente);
        given(passwordService.hash("1234")).willReturn("hashedPassword");
        given(clienteRepository.save(cliente)).willReturn(cliente);
        given(mapper.toResponseDTO(cliente)).willReturn(responseDTO);

        service.create(createDTO);

        then(eventPublisher).should().publishClienteCreado(cliente);
    }

    @Test
    @DisplayName("UT-1.5: findById() retorna DTO cuando el cliente existe")
    void findById_shouldReturnDto_whenExists() {
        given(clienteRepository.findByClienteId(CLIENTE_ID)).willReturn(Optional.of(cliente));
        given(mapper.toResponseDTO(cliente)).willReturn(responseDTO);

        ClienteResponseDTO result = service.findById(CLIENTE_ID);

        assertThat(result.getClienteId()).isEqualTo(CLIENTE_ID);
    }

    @Test
    @DisplayName("UT-1.6: findById() lanza ResourceNotFoundException cuando no existe")
    void findById_shouldThrow_whenNotFound() {
        given(clienteRepository.findByClienteId("nonexistent")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado");
    }

    @Test
    @DisplayName("UT-1.7: findAll() retorna lista de DTOs")
    void findAll_shouldReturnList() {
        given(clienteRepository.findAll()).willReturn(List.of(cliente));
        given(mapper.toResponseDTO(cliente)).willReturn(responseDTO);

        List<ClienteResponseDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClienteId()).isEqualTo(CLIENTE_ID);
    }

    @Test
    @DisplayName("UT-1.8: update() actualiza campos y publica evento cliente.actualizado")
    void update_shouldUpdateAndPublishEvent() {
        ClienteUpdateDTO updateDTO = new ClienteUpdateDTO();
        updateDTO.setNombre("Jose Actualizado");

        given(clienteRepository.findByClienteId(CLIENTE_ID)).willReturn(Optional.of(cliente));
        given(clienteRepository.save(cliente)).willReturn(cliente);
        given(mapper.toResponseDTO(cliente)).willReturn(responseDTO);

        ClienteResponseDTO result = service.update(CLIENTE_ID, updateDTO);

        assertThat(result.getClienteId()).isEqualTo(CLIENTE_ID);
        then(mapper).should().updateEntity(cliente, updateDTO);
        then(eventPublisher).should().publishClienteActualizado(cliente);
    }

    @Test
    @DisplayName("UT-1.9: patch() actualiza solo los campos enviados")
    void patch_shouldUpdateOnlyProvidedFields() {
        ClientePatchDTO patchDTO = new ClientePatchDTO();
        patchDTO.setEstado(false);

        given(clienteRepository.findByClienteId(CLIENTE_ID)).willReturn(Optional.of(cliente));
        given(clienteRepository.save(cliente)).willReturn(cliente);
        given(mapper.toResponseDTO(cliente)).willReturn(responseDTO);

        ClienteResponseDTO result = service.patch(CLIENTE_ID, patchDTO);

        assertThat(result.getClienteId()).isEqualTo(CLIENTE_ID);
        then(mapper).should().patchEntity(cliente, patchDTO);
        then(eventPublisher).should().publishClienteActualizado(cliente);
    }

    @Test
    @DisplayName("UT-1.10: delete() elimina el cliente")
    void delete_shouldRemoveCliente() {
        given(clienteRepository.findByClienteId(CLIENTE_ID)).willReturn(Optional.of(cliente));

        service.delete(CLIENTE_ID);

        then(clienteRepository).should().delete(cliente);
    }

    @Test
    @DisplayName("UT-1.11: delete() lanza ResourceNotFoundException cuando no existe")
    void delete_shouldThrow_whenNotFound() {
        given(clienteRepository.findByClienteId("nonexistent")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado");
    }
}
