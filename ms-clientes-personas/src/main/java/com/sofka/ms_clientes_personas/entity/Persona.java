package com.sofka.ms_clientes_personas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "personas")
@Inheritance(strategy = InheritanceType.JOINED)
public class Persona extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @NotBlank
    private String nombre;

    @Column(length = 20)
    private String genero;

    private Integer edad;

    @Column(nullable = false, unique = true, length = 20)
    @NotBlank
    private String identificacion;

    @Column(nullable = false, length = 300)
    @NotBlank
    private String direccion;

    @Column(nullable = false, length = 20)
    @NotBlank
    private String telefono;
}
