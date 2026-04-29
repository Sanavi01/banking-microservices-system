package com.sofka.ms_cuentas_movimientos.service;

import com.sofka.ms_cuentas_movimientos.dto.ReporteRequest;
import com.sofka.ms_cuentas_movimientos.dto.ReporteResponseDTO;
import com.sofka.ms_cuentas_movimientos.entity.Cliente;
import com.sofka.ms_cuentas_movimientos.entity.Cuenta;
import com.sofka.ms_cuentas_movimientos.entity.Movimiento;
import com.sofka.ms_cuentas_movimientos.exception.InvalidDateRangeException;
import com.sofka.ms_cuentas_movimientos.exception.ResourceNotFoundException;
import com.sofka.ms_cuentas_movimientos.repository.ClienteRepository;
import com.sofka.ms_cuentas_movimientos.repository.MovimientoRepository;
import com.sofka.ms_cuentas_movimientos.service.impl.ReporteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReporteService Unit Tests")
class ReporteServiceTest {

    @Mock private MovimientoRepository movimientoRepository;
    @Mock private ClienteRepository clienteRepository;
    @InjectMocks private ReporteServiceImpl service;

    private ReporteRequest request;
    private Cliente cliente;
    private Cuenta cuenta;
    private Movimiento movimiento;
    private static final String CLIENTE_ID = "client-123";

    @BeforeEach
    void setUp() {
        request = new ReporteRequest();
        request.setClienteId(CLIENTE_ID);
        request.setFechaInicio(LocalDate.of(2022, 2, 1));
        request.setFechaFin(LocalDate.of(2022, 2, 28));

        cliente = new Cliente();
        cliente.setClienteId(CLIENTE_ID);
        cliente.setNombre("Marianela Montalvo");
        cliente.setEstado(true);

        cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setNumeroCuenta("225487");
        cuenta.setTipoCuenta("Corriente");
        cuenta.setSaldoInicial(new BigDecimal("100.00"));
        cuenta.setEstado(true);
        cuenta.setClienteId(CLIENTE_ID);

        movimiento = new Movimiento();
        movimiento.setId(1L);
        movimiento.setCuenta(cuenta);
        movimiento.setValor(new BigDecimal("600.00"));
        movimiento.setSaldo(new BigDecimal("700.00"));
        movimiento.setTipoMovimiento("Depósito");
        movimiento.setFecha(LocalDateTime.of(2022, 2, 10, 11, 0));
    }

    @Test
    @DisplayName("UT-4.1: Reporte con datos válidos retorna lista de DTOs")
    void generarReporte_shouldReturnList_whenClienteYFechasValidos() {
        given(clienteRepository.findById(CLIENTE_ID)).willReturn(Optional.of(cliente));
        given(movimientoRepository.findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc(
                CLIENTE_ID, request.getFechaInicio().atStartOfDay(),
                request.getFechaFin().atTime(23, 59, 59)))
                .willReturn(List.of(movimiento));

        List<ReporteResponseDTO> result = service.generarReporte(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCliente()).isEqualTo("Marianela Montalvo");
        assertThat(result.get(0).getNumeroCuenta()).isEqualTo("225487");
    }

    @Test
    @DisplayName("UT-4.2: Sin movimientos en el rango retorna lista vacía")
    void generarReporte_shouldReturnEmptyList_whenNoMovimientosInRange() {
        given(clienteRepository.findById(CLIENTE_ID)).willReturn(Optional.of(cliente));
        given(movimientoRepository.findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc(
                CLIENTE_ID, request.getFechaInicio().atStartOfDay(),
                request.getFechaFin().atTime(23, 59, 59)))
                .willReturn(List.of());

        List<ReporteResponseDTO> result = service.generarReporte(request);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("UT-4.3: Cliente inexistente lanza ResourceNotFoundException")
    void generarReporte_shouldThrow_whenClienteNotFound() {
        given(clienteRepository.findById("no-existe")).willReturn(Optional.empty());
        request.setClienteId("no-existe");

        assertThatThrownBy(() -> service.generarReporte(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado");
    }

    @Test
    @DisplayName("UT-4.4: fechaInicio > fechaFin lanza InvalidDateRangeException")
    void generarReporte_shouldThrow_whenFechaInicioMayorFechaFin() {
        request.setFechaInicio(LocalDate.of(2022, 2, 28));
        request.setFechaFin(LocalDate.of(2022, 2, 1));

        assertThatThrownBy(() -> service.generarReporte(request))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("fechaInicio no puede ser mayor");
    }

    @Test
    @DisplayName("UT-4.5: Campos del DTO coinciden con @JsonProperty español")
    void generarReporte_shouldMapFieldsCorrectly_toDTO() {
        given(clienteRepository.findById(CLIENTE_ID)).willReturn(Optional.of(cliente));
        given(movimientoRepository.findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc(
                CLIENTE_ID, request.getFechaInicio().atStartOfDay(),
                request.getFechaFin().atTime(23, 59, 59)))
                .willReturn(List.of(movimiento));

        List<ReporteResponseDTO> result = service.generarReporte(request);

        ReporteResponseDTO dto = result.get(0);
        assertThat(dto.getFecha()).isEqualTo("10/02/2022");
        assertThat(dto.getCliente()).isEqualTo("Marianela Montalvo");
        assertThat(dto.getNumeroCuenta()).isEqualTo("225487");
        assertThat(dto.getTipo()).isEqualTo("Corriente");
        assertThat(dto.getSaldoInicial()).isEqualByComparingTo("100.00");
        assertThat(dto.getMovimiento()).isEqualByComparingTo("600.00");
        assertThat(dto.getSaldoDisponible()).isEqualByComparingTo("700.00");
    }

    @Test
    @DisplayName("UT-4.6: Resultados ordenados por fecha ascendente")
    void generarReporte_shouldOrderByFechaAsc() {
        Movimiento m1 = new Movimiento();
        m1.setCuenta(cuenta);
        m1.setValor(new BigDecimal("100"));
        m1.setSaldo(new BigDecimal("200"));
        m1.setTipoMovimiento("Depósito");
        m1.setFecha(LocalDateTime.of(2022, 2, 15, 10, 0));

        Movimiento m2 = new Movimiento();
        m2.setCuenta(cuenta);
        m2.setValor(new BigDecimal("200"));
        m2.setSaldo(new BigDecimal("400"));
        m2.setTipoMovimiento("Depósito");
        m2.setFecha(LocalDateTime.of(2022, 2, 5, 10, 0));

        given(clienteRepository.findById(CLIENTE_ID)).willReturn(Optional.of(cliente));
        given(movimientoRepository.findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc(
                CLIENTE_ID, request.getFechaInicio().atStartOfDay(),
                request.getFechaFin().atTime(23, 59, 59)))
                .willReturn(List.of(m2, m1)); // Mock returns in ascending order

        List<ReporteResponseDTO> result = service.generarReporte(request);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFecha()).isEqualTo("05/02/2022");
        assertThat(result.get(1).getFecha()).isEqualTo("15/02/2022");
    }

    @Test
    @DisplayName("UT-4.7: Movimientos en las fechas límite se incluyen")
    void generarReporte_shouldIncludeEdgeDates() {
        Movimiento edge = new Movimiento();
        edge.setCuenta(cuenta);
        edge.setValor(new BigDecimal("50"));
        edge.setSaldo(new BigDecimal("150"));
        edge.setTipoMovimiento("Depósito");
        edge.setFecha(LocalDateTime.of(2022, 2, 1, 0, 0));

        given(clienteRepository.findById(CLIENTE_ID)).willReturn(Optional.of(cliente));
        given(movimientoRepository.findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc(
                CLIENTE_ID, request.getFechaInicio().atStartOfDay(),
                request.getFechaFin().atTime(23, 59, 59)))
                .willReturn(List.of(edge));

        List<ReporteResponseDTO> result = service.generarReporte(request);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("UT-4.8: Solo retorna movimientos del cliente especificado")
    void generarReporte_shouldFilterByClienteId_onlyThatCliente() {
        given(clienteRepository.findById(CLIENTE_ID)).willReturn(Optional.of(cliente));
        given(movimientoRepository.findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc(
                CLIENTE_ID, request.getFechaInicio().atStartOfDay(),
                request.getFechaFin().atTime(23, 59, 59)))
                .willReturn(List.of(movimiento));

        List<ReporteResponseDTO> result = service.generarReporte(request);

        then(clienteRepository).should().findById(CLIENTE_ID);
        then(movimientoRepository).should().findByCuentaClienteIdAndFechaBetweenOrderByFechaAsc(
                CLIENTE_ID, request.getFechaInicio().atStartOfDay(),
                request.getFechaFin().atTime(23, 59, 59));
        assertThat(result).allMatch(r -> r.getCliente().equals("Marianela Montalvo"));
    }
}
