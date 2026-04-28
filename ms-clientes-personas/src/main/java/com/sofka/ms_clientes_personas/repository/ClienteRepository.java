package com.sofka.ms_clientes_personas.repository;

import com.sofka.ms_clientes_personas.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByClienteId(String clienteId);
    boolean existsByClienteId(String clienteId);
}
