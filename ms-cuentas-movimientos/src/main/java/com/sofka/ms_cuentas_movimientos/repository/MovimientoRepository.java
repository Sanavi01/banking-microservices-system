package com.sofka.ms_cuentas_movimientos.repository;

import com.sofka.ms_cuentas_movimientos.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM Movimiento m WHERE m.cuenta.id = :cuentaId")
    Optional<BigDecimal> sumValorByCuentaId(@Param("cuentaId") Long cuentaId);
}
