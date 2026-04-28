package com.sofka.ms_clientes_personas.repository;

import com.sofka.ms_clientes_personas.entity.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ClienteRepository Tests")
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    private String savedClienteId;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Jose Lema");
        cliente.setGenero("Masculino");
        cliente.setEdad(30);
        cliente.setIdentificacion("1234567890");
        cliente.setDireccion("Otavalo sn y principal");
        cliente.setTelefono("098254785");
        cliente.setContrasena("hashedPassword");
        cliente.setEstado(true);

        Cliente saved = clienteRepository.save(cliente);
        savedClienteId = saved.getClienteId();
    }

    @Test
    @DisplayName("UT-1.12: save() persiste cliente y retorna entidad con ID")
    void save_shouldPersistCliente() {
        Cliente nuevo = new Cliente();
        nuevo.setNombre("Otro Cliente");
        nuevo.setGenero("Femenino");
        nuevo.setEdad(25);
        nuevo.setIdentificacion("9999999999");
        nuevo.setDireccion("Otra Direccion");
        nuevo.setTelefono("0999999999");
        nuevo.setContrasena("otraPassword");
        nuevo.setEstado(false);

        Cliente saved = clienteRepository.save(nuevo);

        assertThat(saved).isNotNull();
        assertThat(saved.getClienteId()).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNombre()).isEqualTo("Otro Cliente");
        assertThat(saved.getEstado()).isFalse();
    }

    @Test
    @DisplayName("UT-1.13: findByClienteId() retorna entidad cuando existe")
    void findByClienteId_shouldReturnCliente() {
        Optional<Cliente> found = clienteRepository.findByClienteId(savedClienteId);

        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("Jose Lema");
        assertThat(found.get().getIdentificacion()).isEqualTo("1234567890");
    }
}
