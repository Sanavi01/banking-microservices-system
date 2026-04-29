package com.sofka.ms_cuentas_movimientos;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class MsCuentasMovimientosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsCuentasMovimientosApplication.class, args);
	}

}
