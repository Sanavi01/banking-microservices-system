package com.sofka.ms_cuentas_movimientos.repository;

import com.sofka.ms_cuentas_movimientos.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
}
