package com.sofka.ms_cuentas_movimientos.messaging;

import com.sofka.ms_cuentas_movimientos.entity.Cliente;
import com.sofka.ms_cuentas_movimientos.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteEventConsumer {

    private final ClienteRepository clienteRepository;

    @RabbitListener(queues = "${banking.rabbitmq.queue}")
    @Transactional
    public void handleClienteEvent(String json) {
        String evento = extractField(json, "evento");
        String clienteId = extractField(json, "clienteId");
        String nombre = extractField(json, "nombre");
        String estadoStr = extractField(json, "estado");
        Boolean estado = "true".equals(estadoStr);

        switch (evento) {
            case "CLIENTE_CREADO" -> {
                Cliente cliente = new Cliente();
                cliente.setClienteId(clienteId);
                cliente.setNombre(nombre);
                cliente.setEstado(estado);
                clienteRepository.save(cliente);
                log.info("Cliente replicado: {} - {}", clienteId, nombre);
            }
            case "CLIENTE_ACTUALIZADO" -> {
                clienteRepository.findById(clienteId).ifPresent(c -> {
                    c.setNombre(nombre);
                    c.setEstado(estado);
                    clienteRepository.save(c);
                    log.info("Cliente actualizado: {} - {}", clienteId, nombre);
                });
            }
            default -> log.warn("Evento desconocido: {}", evento);
        }
    }

    private String extractField(String json, String field) {
        String key = "\"" + field + "\":";
        int start = json.indexOf(key);
        if (start == -1) return "";
        start += key.length();
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf('"', start);
            return json.substring(start, end);
        } else {
            int end = json.indexOf(',', start);
            if (end == -1) end = json.indexOf('}', start);
            return json.substring(start, end).trim();
        }
    }
}
