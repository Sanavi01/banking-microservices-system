package com.sofka.ms_cuentas_movimientos.messaging;

import com.sofka.ms_cuentas_movimientos.entity.Cliente;
import com.sofka.ms_cuentas_movimientos.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteEventConsumer Tests")
class ClienteEventConsumerTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteEventConsumer consumer;

    @Test
    @DisplayName("UT-2.12: Evento CLIENTE_CREADO replica cliente en BD local")
    void shouldReplicateCliente_onClienteCreadoEvent() {
        String json = "{\"clienteId\":\"abc-123\",\"nombre\":\"Jose Lema\",\"estado\":true,\"evento\":\"CLIENTE_CREADO\",\"timestamp\":\"2026-04-28T10:30:00\"}";

        consumer.handleClienteEvent(json);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        then(clienteRepository).should().save(captor.capture());
        assertThat(captor.getValue().getClienteId()).isEqualTo("abc-123");
        assertThat(captor.getValue().getNombre()).isEqualTo("Jose Lema");
        assertThat(captor.getValue().getEstado()).isTrue();
    }
}
