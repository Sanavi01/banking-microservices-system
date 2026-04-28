package com.sofka.ms_clientes_personas.rabbit;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class ClienteEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String clienteId;
    private String nombre;
    private Boolean estado;
    private String evento;
    private String timestamp;

    public ClienteEventDTO(String clienteId, String nombre, Boolean estado, String evento, LocalDateTime timestamp) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.estado = estado;
        this.evento = evento;
        this.timestamp = timestamp != null ? timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
}
