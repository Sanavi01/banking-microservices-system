package com.sofka.ms_cuentas_movimientos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class ReporteRequest {

    @NotNull(message = "fechaInicio es obligatorio")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate fechaInicio;

    @NotNull(message = "fechaFin es obligatorio")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate fechaFin;

    @NotBlank(message = "clienteId es obligatorio")
    private String clienteId;
}
