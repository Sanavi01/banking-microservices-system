package com.sofka.ms_clientes_personas.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteUpdateDTO {

    @Size(max = 200)
    private String nombre;

    @Size(max = 20)
    private String genero;

    @Min(0) @Max(150)
    private Integer edad;

    @Size(max = 20)
    private String identificacion;

    @Size(max = 300)
    private String direccion;

    @Size(max = 20)
    private String telefono;

    @Size(min = 4, max = 100)
    private String contrasena;

    private Boolean estado;
}
