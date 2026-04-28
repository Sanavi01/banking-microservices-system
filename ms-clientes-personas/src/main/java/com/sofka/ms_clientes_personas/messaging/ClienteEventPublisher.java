package com.sofka.ms_clientes_personas.messaging;

import com.sofka.ms_clientes_personas.entity.Cliente;
import com.sofka.ms_clientes_personas.rabbit.ClienteEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ClienteEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${banking.rabbitmq.exchange}")
    private String exchange;

    @Value("${banking.rabbitmq.routing.cliente-creado}")
    private String routingKeyCreado;

    @Value("${banking.rabbitmq.routing.cliente-actualizado}")
    private String routingKeyActualizado;

    public void publishClienteCreado(Cliente cliente) {
        ClienteEventDTO event = new ClienteEventDTO(
                cliente.getClienteId(),
                cliente.getNombre(),
                cliente.getEstado(),
                "CLIENTE_CREADO",
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(exchange, routingKeyCreado, event);
    }

    public void publishClienteActualizado(Cliente cliente) {
        ClienteEventDTO event = new ClienteEventDTO(
                cliente.getClienteId(),
                cliente.getNombre(),
                cliente.getEstado(),
                "CLIENTE_ACTUALIZADO",
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(exchange, routingKeyActualizado, event);
    }
}
