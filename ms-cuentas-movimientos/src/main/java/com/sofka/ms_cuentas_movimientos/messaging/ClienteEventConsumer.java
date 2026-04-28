package com.sofka.ms_cuentas_movimientos.messaging;

import com.sofka.ms_cuentas_movimientos.entity.Cliente;
import com.sofka.ms_cuentas_movimientos.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteEventConsumer {

    private final ClienteRepository clienteRepository;

    @RabbitListener(queues = "${banking.rabbitmq.queue}")
    @Transactional
    public void handleClienteEvent(Map<String, Object> event) {
        String evento = (String) event.get("evento");
        String clienteId = (String) event.get("clienteId");
        String nombre = (String) event.get("nombre");
        Boolean estado = event.get("estado") != null ? (Boolean) event.get("estado") : true;

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
}
