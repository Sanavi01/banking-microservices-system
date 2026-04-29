package com.sofka.ms_cuentas_movimientos.repository;

import com.sofka.ms_cuentas_movimientos.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM Movimiento m WHERE m.cuenta.id = :cuentaId")
    Optional<BigDecimal> sumValorByCuentaId(@Param("cuentaId") Long cuentaId);

    @Query("SELECT m FROM Movimiento m " +
           "JOIN FETCH m.cuenta c " +
           "WHERE c.clienteId = :clienteId " +
           "AND m.fecha BETWEEN :inicio AND :fin " +
           "ORDER BY m.fecha ASC")
    List<Movimiento> findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc(
            @Param("clienteId") String clienteId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
