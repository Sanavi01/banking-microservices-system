package com.sofka.ms_clientes_personas.messaging;

import com.sofka.ms_clientes_personas.entity.Cliente;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        String json = buildJson(cliente, "CLIENTE_CREADO");
        rabbitTemplate.convertAndSend(exchange, routingKeyCreado, json);
    }

    public void publishClienteActualizado(Cliente cliente) {
        String json = buildJson(cliente, "CLIENTE_ACTUALIZADO");
        rabbitTemplate.convertAndSend(exchange, routingKeyActualizado, json);
    }

    private String buildJson(Cliente cliente, String evento) {
        return String.format(
            "{\"clienteId\":\"%s\",\"nombre\":\"%s\",\"estado\":%s,\"evento\":\"%s\",\"timestamp\":\"%s\"}",
            cliente.getClienteId(),
            escape(cliente.getNombre()),
            cliente.getEstado(),
            evento,
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }
}
