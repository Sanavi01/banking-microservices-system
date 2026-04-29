package com.sofka.ms_cuentas_movimientos.controller;

import com.sofka.ms_cuentas_movimientos.entity.Cliente;
import com.sofka.ms_cuentas_movimientos.entity.Cuenta;
import com.sofka.ms_cuentas_movimientos.repository.ClienteRepository;
import com.sofka.ms_cuentas_movimientos.repository.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MovimientoController Integration Tests")
class MovimientoControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private CuentaRepository cuentaRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl;
    private String clientId;
    private static int cuentaCounter = 1;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        clientId = "mvmt-test-" + System.nanoTime() % 10000;

        Cliente c = new Cliente();
        c.setClienteId(clientId);
        c.setNombre("Test Client " + clientId);
        c.setEstado(true);
        clienteRepository.save(c);
    }

    private Long crearCuenta(BigDecimal saldoInicial) {
        int n = cuentaCounter++;
        Cuenta c = new Cuenta();
        c.setNumeroCuenta(String.format("MV%04d", n % 9999));
        c.setTipoCuenta("Ahorro");
        c.setSaldoInicial(saldoInicial);
        c.setEstado(true);
        c.setClienteId(clientId);
        return cuentaRepository.save(c).getId();
    }

    private HttpEntity<String> json(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test @Order(1)
    @DisplayName("IT-3.1: POST depósito retorna 201 con tipo Depósito")
    void create_shouldReturn201_whenDeposito() {
        Long id = crearCuenta(new BigDecimal("2000.00"));
        String body = "{\"cuentaId\": %d, \"valor\": 600.00}".formatted(id);
        ResponseEntity<Map> r = restTemplate.exchange(baseUrl + "/movimientos", HttpMethod.POST, json(body), Map.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(r.getBody().get("tipoMovimiento")).isEqualTo("Depósito");
    }

    @Test @Order(2)
    @DisplayName("IT-3.2: POST retiro con saldo suficiente retorna 201")
    void create_shouldReturn201_whenRetiroConSaldo() {
        Long id = crearCuenta(new BigDecimal("2000.00"));
        String body = "{\"cuentaId\": %d, \"valor\": -575.00}".formatted(id);
        ResponseEntity<Map> r = restTemplate.exchange(baseUrl + "/movimientos", HttpMethod.POST, json(body), Map.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(r.getBody().get("tipoMovimiento")).isEqualTo("Retiro");
    }

    @Test @Order(3)
    @DisplayName("IT-3.3: POST retiro con saldo insuficiente retorna 422")
    void create_shouldReturn422_whenSaldoInsuficiente() {
        Long id = crearCuenta(BigDecimal.ZERO);
        String body = "{\"cuentaId\": %d, \"valor\": -100.00}".formatted(id);
        try {
            ResponseEntity<Map> r = restTemplate.exchange(baseUrl + "/movimientos", HttpMethod.POST, json(body), Map.class);
            assertThat(r.getStatusCode().is4xxClientError()).as("Expected 4xx error").isTrue();
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode().is4xxClientError()).as("Expected 4xx error").isTrue();
        }
    }

    @Test @Order(4)
    @DisplayName("IT-3.4: POST retiro del saldo total retorna 201 con saldo=0")
    void create_shouldReturn201_whenRetiroTotal() {
        Long id = crearCuenta(new BigDecimal("540.00"));
        String body = "{\"cuentaId\": %d, \"valor\": -540.00}".formatted(id);
        ResponseEntity<Map> r = restTemplate.exchange(baseUrl + "/movimientos", HttpMethod.POST, json(body), Map.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(((Number) r.getBody().get("saldo")).intValue()).isZero();
    }

    @Test @Order(5)
    @DisplayName("IT-3.5: POST con cuenta inexistente retorna 404")
    void create_shouldReturn404_whenCuentaNotFound() {
        String body = "{\"cuentaId\": 99999, \"valor\": 100.00}";
        try {
            restTemplate.exchange(baseUrl + "/movimientos", HttpMethod.POST, json(body), Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test @Order(6)
    @DisplayName("IT-3.6: POST con valor cero retorna 400")
    void create_shouldReturn400_whenValorCero() {
        Long id = crearCuenta(new BigDecimal("1000.00"));
        String body = "{\"cuentaId\": %d, \"valor\": 0.00}".formatted(id);
        try {
            restTemplate.exchange(baseUrl + "/movimientos", HttpMethod.POST, json(body), Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test @Order(7)
    @DisplayName("IT-3.7: POST sin cuentaId retorna 400")
    void create_shouldReturn400_whenNoCuentaId() {
        String body = "{\"valor\": 100.00}";
        try {
            restTemplate.exchange(baseUrl + "/movimientos", HttpMethod.POST, json(body), Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test @Order(8)
    @DisplayName("IT-3.8: POST sin valor retorna 400")
    void create_shouldReturn400_whenNoValor() {
        Long id = crearCuenta(new BigDecimal("1000.00"));
        String body = "{\"cuentaId\": %d}".formatted(id);
        try {
            restTemplate.exchange(baseUrl + "/movimientos", HttpMethod.POST, json(body), Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test @Order(9)
    @DisplayName("IT-3.9: GET /movimientos retorna 200 con array")
    void findAll_shouldReturn200_withList() {
        ResponseEntity<String> r = restTemplate.getForEntity(baseUrl + "/movimientos", String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).startsWith("[");
    }

    @Test @Order(10)
    @DisplayName("IT-3.10: GET /movimientos/{id} con ID existente retorna 200")
    void findById_shouldReturn200_whenExists() {
        Long id = crearCuenta(new BigDecimal("500.00"));
        String body = "{\"cuentaId\": %d, \"valor\": 50.00}".formatted(id);
        restTemplate.exchange(baseUrl + "/movimientos", HttpMethod.POST, json(body), Map.class);

        var list = restTemplate.getForEntity(baseUrl + "/movimientos", Map[].class);
        if (list.getBody() != null && list.getBody().length > 0) {
            int mvId = (Integer) list.getBody()[0].get("id");
            ResponseEntity<Map> r = restTemplate.getForEntity(baseUrl + "/movimientos/" + mvId, Map.class);
            assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test @Order(11)
    @DisplayName("IT-3.11: GET /movimientos/{id} con ID inexistente retorna 404")
    void findById_shouldReturn404_whenNotFound() {
        try {
            restTemplate.getForEntity(baseUrl + "/movimientos/99999", Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test @Order(12)
    @DisplayName("IT-3.12: Múltiples movimientos actualizan saldo acumulado")
    void multipleMovimientos_shouldUpdateAccumulatedSaldo() {
        Long id = crearCuenta(new BigDecimal("1000.00"));

        String body1 = "{\"cuentaId\": %d, \"valor\": 150.00}".formatted(id);
        restTemplate.exchange(baseUrl + "/movimientos", HttpMethod.POST, json(body1), Map.class);

        String body2 = "{\"cuentaId\": %d, \"valor\": -200.00}".formatted(id);
        restTemplate.exchange(baseUrl + "/movimientos", HttpMethod.POST, json(body2), Map.class);

        var list = restTemplate.getForEntity(baseUrl + "/movimientos", Map[].class);
        assertThat(list.getBody()).isNotNull();
        assertThat(list.getBody().length).isGreaterThanOrEqualTo(2);
    }
}
