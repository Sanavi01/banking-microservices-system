package com.sofka.ms_clientes_personas.dto;

import lombok.Data;

@Data
public class ClientePatchDTO {
    private String nombre;
    private String genero;
    private Integer edad;
    private String identificacion;
    private String direccion;
    private String telefono;
    private String contrasena;
    private Boolean estado;
}
