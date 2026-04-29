package com.sofka.ms_clientes_personas;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class MsClientesPersonasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsClientesPersonasApplication.class, args);
	}

}
