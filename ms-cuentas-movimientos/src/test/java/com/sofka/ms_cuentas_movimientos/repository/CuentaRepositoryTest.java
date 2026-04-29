package com.sofka.ms_cuentas_movimientos.repository;

import com.sofka.ms_cuentas_movimientos.entity.Cuenta;
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
@DisplayName("CuentaRepository Tests")
class CuentaRepositoryTest {

    @Autowired
    private CuentaRepository cuentaRepository;

    private Cuenta savedCuenta;

    @BeforeEach
    void setUp() {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("478758");
        cuenta.setTipoCuenta("Ahorro");
        cuenta.setSaldoInicial(new BigDecimal("2000.00"));
        cuenta.setEstado(true);
        cuenta.setClienteId("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        savedCuenta = cuentaRepository.save(cuenta);
    }

    @Test
    @DisplayName("UT-2.10: save() persiste y retorna entidad con ID")
    void save_shouldPersistCuenta() {
        Cuenta nueva = new Cuenta();
        nueva.setNumeroCuenta("999999");
        nueva.setTipoCuenta("Corriente");
        nueva.setSaldoInicial(new BigDecimal("500.00"));
        nueva.setEstado(false);
        nueva.setClienteId("b2c3d4e5-f6a7-8901-bcde-f12345678901");

        Cuenta saved = cuentaRepository.save(nueva);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNumeroCuenta()).isEqualTo("999999");
        assertThat(saved.getEstado()).isFalse();
    }

    @Test
    @DisplayName("UT-2.11: findByNumeroCuenta() retorna entidad cuando existe")
    void findByNumeroCuenta_shouldReturnCuenta() {
        Optional<Cuenta> found = cuentaRepository.findByNumeroCuenta("478758");

        assertThat(found).isPresent();
        assertThat(found.get().getTipoCuenta()).isEqualTo("Ahorro");
        assertThat(found.get().getSaldoInicial()).isEqualByComparingTo("2000.00");
    }
}
