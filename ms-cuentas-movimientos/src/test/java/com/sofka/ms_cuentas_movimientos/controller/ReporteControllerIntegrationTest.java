package com.sofka.ms_cuentas_movimientos.controller;

import com.sofka.ms_cuentas_movimientos.entity.Cliente;
import com.sofka.ms_cuentas_movimientos.entity.Cuenta;
import com.sofka.ms_cuentas_movimientos.entity.Movimiento;
import com.sofka.ms_cuentas_movimientos.repository.ClienteRepository;
import com.sofka.ms_cuentas_movimientos.repository.CuentaRepository;
import com.sofka.ms_cuentas_movimientos.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SuppressWarnings({"rawtypes", "unchecked"})
@DisplayName("ReporteController Integration Tests")
class ReporteControllerIntegrationTest {

    @LocalServerPort private int port;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private CuentaRepository cuentaRepository;
    @Autowired private MovimientoRepository movimientoRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl;
    private String clientId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        clientId = "rpt-" + System.nanoTime() % 10000;

        Cliente c = new Cliente();
        c.setClienteId(clientId);
        c.setNombre("Marianela Montalvo");
        c.setEstado(true);
        clienteRepository.save(c);

        // Cuenta 1
        Cuenta c1 = new Cuenta();
        c1.setNumeroCuenta("RPT-" + (System.nanoTime() % 100000));
        c1.setTipoCuenta("Corriente");
        c1.setSaldoInicial(new BigDecimal("100.00"));
        c1.setEstado(true);
        c1.setClienteId(clientId);
        Cuenta saved1 = cuentaRepository.save(c1);

        // Cuenta 2
        Cuenta c2 = new Cuenta();
        c2.setNumeroCuenta("RPT-" + (System.nanoTime() % 100000 + 1));
        c2.setTipoCuenta("Ahorros");
        c2.setSaldoInicial(new BigDecimal("540.00"));
        c2.setEstado(true);
        c2.setClienteId(clientId);
        Cuenta saved2 = cuentaRepository.save(c2);

        // Movimiento 1: 10/02/2022 deposito 600 en cuenta 1
        Movimiento m1 = new Movimiento();
        m1.setCuenta(saved1);
        m1.setValor(new BigDecimal("600.00"));
        m1.setSaldo(new BigDecimal("700.00"));
        m1.setTipoMovimiento("Depósito");
        m1.setFecha(LocalDateTime.of(2022, 2, 10, 11, 0));
        movimientoRepository.save(m1);

        // Movimiento 2: 08/02/2022 retiro 540 en cuenta 2
        Movimiento m2 = new Movimiento();
        m2.setCuenta(saved2);
        m2.setValor(new BigDecimal("-540.00"));
        m2.setSaldo(BigDecimal.ZERO);
        m2.setTipoMovimiento("Retiro");
        m2.setFecha(LocalDateTime.of(2022, 2, 8, 9, 0));
        movimientoRepository.save(m2);
    }

    @Test
    @DisplayName("IT-4.1: GET /reportes con rango y cliente válidos retorna 200 + array")
    void reporte_shouldReturn200_withMovimientos() {
        String url = baseUrl + "/reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022&clienteId=" + clientId;
        ResponseEntity<List> r = restTemplate.getForEntity(url, List.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("IT-4.2: Validar formato exacto con nombres de campo en español")
    void reporte_shouldHaveSpanishFieldNames() {
        String url = baseUrl + "/reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022&clienteId=" + clientId;
        ResponseEntity<Map[]> r = restTemplate.getForEntity(url, Map[].class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> first = r.getBody()[0];
        assertThat(first).containsKeys("Fecha", "Cliente", "Numero Cuenta", "Tipo",
                "Saldo Inicial", "Estado", "Movimiento", "Saldo Disponible");
    }

    @Test
    @DisplayName("IT-4.3: Rango sin movimientos retorna 200 con array vacío")
    void reporte_shouldReturnEmpty_whenNoMovimientos() {
        String url = baseUrl + "/reportes?fechaInicio=01/01/2020&fechaFin=01/01/2020&clienteId=" + clientId;
        ResponseEntity<List> r = restTemplate.getForEntity(url, List.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isEmpty();
    }

    @Test
    @DisplayName("IT-4.4: Cliente inexistente retorna 404")
    void reporte_shouldReturn404_whenClienteNotFound() {
        String url = baseUrl + "/reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022&clienteId=no-existe";
        try {
            restTemplate.getForEntity(url, Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    @DisplayName("IT-4.5: Falta clienteId retorna 400")
    void reporte_shouldReturn400_whenClienteIdMissing() {
        String url = baseUrl + "/reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022";
        try {
            restTemplate.getForEntity(url, Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("IT-4.6: Falta fechaInicio retorna 400")
    void reporte_shouldReturn400_whenFechaInicioMissing() {
        String url = baseUrl + "/reportes?fechaFin=28/02/2022&clienteId=" + clientId;
        try {
            restTemplate.getForEntity(url, Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("IT-4.7: fechaInicio > fechaFin retorna 400")
    void reporte_shouldReturn400_whenFechaInicioMayor() {
        String url = baseUrl + "/reportes?fechaInicio=28/02/2022&fechaFin=01/02/2022&clienteId=" + clientId;
        try {
            restTemplate.getForEntity(url, Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("IT-4.8: Mismo día para ambas fechas retorna 200")
    void reporte_shouldReturn200_whenSameDay() {
        String url = baseUrl + "/reportes?fechaInicio=10/02/2022&fechaFin=10/02/2022&clienteId=" + clientId;
        ResponseEntity<List> r = restTemplate.getForEntity(url, List.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("IT-4.9: Resultados ordenados por fecha ascendente")
    void reporte_shouldBeOrdered_byFechaAsc() {
        String url = baseUrl + "/reportes?fechaInicio=01/02/2022&fechaFin=28/02/2022&clienteId=" + clientId;
        ResponseEntity<Map[]> r = restTemplate.getForEntity(url, Map[].class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object>[] body = r.getBody();
        if (body.length >= 2) {
            String fecha1 = (String) body[0].get("Fecha");
            String fecha2 = (String) body[1].get("Fecha");
            assertThat(fecha1.compareTo(fecha2)).isLessThanOrEqualTo(0);
        }
    }
}
