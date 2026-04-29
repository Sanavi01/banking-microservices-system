package com.sofka.ms_cuentas_movimientos.repository;

import com.sofka.ms_cuentas_movimientos.entity.Cuenta;
import com.sofka.ms_cuentas_movimientos.entity.Movimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("MovimientoRepository Tests")
class MovimientoRepositoryTest {

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    private Long cuentaId;

    @BeforeEach
    void setUp() {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("REPO-CUENTA-1");
        cuenta.setTipoCuenta("Ahorro");
        cuenta.setSaldoInicial(new BigDecimal("1000.00"));
        cuenta.setEstado(true);
        cuenta.setClienteId("repo-client-1");
        Cuenta saved = cuentaRepository.save(cuenta);
        cuentaId = saved.getId();
    }

    @Test
    @DisplayName("UT-3.13: save() persiste el movimiento con fecha y saldo")
    void save_shouldPersistMovimiento() {
        Cuenta cuenta = cuentaRepository.findById(cuentaId).orElseThrow();
        Movimiento mov = new Movimiento();
        mov.setCuenta(cuenta);
        mov.setValor(new BigDecimal("500.00"));
        mov.setSaldo(new BigDecimal("1500.00"));
        mov.setTipoMovimiento("Depósito");

        Movimiento saved = movimientoRepository.save(mov);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFecha()).isNotNull();
        assertThat(saved.getTipoMovimiento()).isEqualTo("Depósito");
        assertThat(saved.getValor()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("UT-3.14: sumValorByCuentaId() retorna la suma correcta")
    void sumValorByCuentaId_shouldReturnSum() {
        Cuenta cuenta = cuentaRepository.findById(cuentaId).orElseThrow();

        Movimiento m1 = new Movimiento();
        m1.setCuenta(cuenta);
        m1.setValor(new BigDecimal("300.00"));
        m1.setSaldo(new BigDecimal("1300.00"));
        m1.setTipoMovimiento("Depósito");
        movimientoRepository.save(m1);

        Movimiento m2 = new Movimiento();
        m2.setCuenta(cuenta);
        m2.setValor(new BigDecimal("-200.00"));
        m2.setSaldo(new BigDecimal("1100.00"));
        m2.setTipoMovimiento("Retiro");
        movimientoRepository.save(m2);

        Optional<BigDecimal> sum = movimientoRepository.sumValorByCuentaId(cuentaId);
        assertThat(sum).isPresent();
        assertThat(sum.get()).isEqualByComparingTo("100.00"); // 300 + (-200) = 100
    }
}
