package com.sofka.ms_clientes_personas.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClienteCreateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    private String nombre;

    @Size(max = 20)
    private String genero;

    @Min(0) @Max(150)
    private Integer edad;

    @NotBlank(message = "La identificaci\u00f3n es obligatoria")
    @Size(max = 20)
    private String identificacion;

    @NotBlank(message = "La direcci\u00f3n es obligatoria")
    @Size(max = 300)
    private String direccion;

    @NotBlank(message = "El tel\u00e9fono es obligatorio")
    @Size(max = 20)
    private String telefono;

    @NotBlank(message = "La contrase\u00f1a es obligatoria")
    @Size(min = 4, max = 100)
    private String contrasena;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
