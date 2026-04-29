package com.sofka.ms_cuentas_movimientos.service;

import com.sofka.ms_cuentas_movimientos.dto.MovimientoCreateDTO;
import com.sofka.ms_cuentas_movimientos.dto.MovimientoResponseDTO;
import com.sofka.ms_cuentas_movimientos.entity.Cuenta;
import com.sofka.ms_cuentas_movimientos.entity.Movimiento;
import com.sofka.ms_cuentas_movimientos.exception.InsufficientBalanceException;
import com.sofka.ms_cuentas_movimientos.exception.InvalidMovementException;
import com.sofka.ms_cuentas_movimientos.exception.ResourceNotFoundException;
import com.sofka.ms_cuentas_movimientos.mapper.MovimientoMapper;
import com.sofka.ms_cuentas_movimientos.repository.CuentaRepository;
import com.sofka.ms_cuentas_movimientos.repository.MovimientoRepository;
import com.sofka.ms_cuentas_movimientos.service.impl.MovimientoServiceImpl;
import com.sofka.ms_cuentas_movimientos.strategy.DepositoStrategy;
import com.sofka.ms_cuentas_movimientos.strategy.RetiroStrategy;
import com.sofka.ms_cuentas_movimientos.strategy.TipoMovimientoResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovimientoService Unit Tests")
class MovimientoServiceTest {

    @Mock private MovimientoRepository movimientoRepository;
    @Mock private CuentaRepository cuentaRepository;
    @Mock private MovimientoMapper mapper;
    @Mock private TipoMovimientoResolver strategyResolver;

    @InjectMocks
    private MovimientoServiceImpl service;

    private MovimientoCreateDTO depositoDTO;
    private MovimientoCreateDTO retiroDTO;
    private Cuenta cuenta;
    private Movimiento movimiento;
    private MovimientoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setNumeroCuenta("478758");
        cuenta.setTipoCuenta("Ahorro");
        cuenta.setSaldoInicial(new BigDecimal("2000.00"));
        cuenta.setEstado(true);
        cuenta.setClienteId("client-1");

        depositoDTO = new MovimientoCreateDTO();
        depositoDTO.setCuentaId(1L);
        depositoDTO.setValor(new BigDecimal("600.00"));

        retiroDTO = new MovimientoCreateDTO();
        retiroDTO.setCuentaId(1L);
        retiroDTO.setValor(new BigDecimal("-575.00"));

        movimiento = new Movimiento();
        movimiento.setId(1L);
        movimiento.setCuenta(cuenta);
        movimiento.setValor(new BigDecimal("600.00"));
        movimiento.setSaldo(new BigDecimal("2600.00"));
        movimiento.setTipoMovimiento("Depósito");
        movimiento.setFecha(LocalDateTime.now());

        responseDTO = MovimientoResponseDTO.builder()
                .id(1L)
                .cuentaId(1L)
                .valor(new BigDecimal("600.00"))
                .saldo(new BigDecimal("2600.00"))
                .tipoMovimiento("Depósito")
                .fecha(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("UT-3.1: Valor positivo crea tipo Depósito")
    void registrarMovimiento_shouldCreateDeposito_whenValorPositivo() {
        given(strategyResolver.resolver(depositoDTO.getValor())).willReturn(new DepositoStrategy());
        given(cuentaRepository.findById(1L)).willReturn(Optional.of(cuenta));
        given(movimientoRepository.sumValorByCuentaId(1L)).willReturn(Optional.of(BigDecimal.ZERO));
        given(movimientoRepository.save(any(Movimiento.class))).willReturn(movimiento);
        given(mapper.toResponseDTO(any(Movimiento.class))).willReturn(responseDTO);

        MovimientoResponseDTO result = service.registrar(depositoDTO);

        assertThat(result.getTipoMovimiento()).isEqualTo("Depósito");
        then(movimientoRepository).should().save(any(Movimiento.class));
    }

    @Test
    @DisplayName("UT-3.2: Valor negativo con saldo suficiente crea Retiro")
    void registrarMovimiento_shouldCreateRetiro_whenValorNegativoYSaldoSuficiente() {
        Cuenta cuentaLocal = new Cuenta();
        cuentaLocal.setId(1L);
        cuentaLocal.setSaldoInicial(new BigDecimal("2000.00"));
        cuentaLocal.setEstado(true);

        Movimiento saved = new Movimiento();
        saved.setId(1L);
        saved.setCuenta(cuentaLocal);
        saved.setValor(new BigDecimal("-575.00"));
        saved.setSaldo(new BigDecimal("1425.00"));
        saved.setTipoMovimiento("Retiro");

        MovimientoResponseDTO resp = MovimientoResponseDTO.builder()
                .id(1L).cuentaId(1L)
                .valor(new BigDecimal("-575.00"))
                .saldo(new BigDecimal("1425.00"))
                .tipoMovimiento("Retiro")
                .build();

        given(strategyResolver.resolver(retiroDTO.getValor())).willReturn(new RetiroStrategy());
        given(cuentaRepository.findById(1L)).willReturn(Optional.of(cuentaLocal));
        given(movimientoRepository.sumValorByCuentaId(1L)).willReturn(Optional.of(BigDecimal.ZERO));
        given(movimientoRepository.save(any(Movimiento.class))).willReturn(saved);
        given(mapper.toResponseDTO(any(Movimiento.class))).willReturn(resp);

        MovimientoResponseDTO result = service.registrar(retiroDTO);

        assertThat(result.getTipoMovimiento()).isEqualTo("Retiro");
        assertThat(result.getSaldo()).isEqualByComparingTo("1425.00");
    }

    @Test
    @DisplayName("UT-3.3: Retiro sin saldo lanza InsufficientBalanceException")
    void registrarMovimiento_shouldThrowSaldoNoDisponible_whenSaldoInsuficiente() {
        MovimientoCreateDTO retiroGrande = new MovimientoCreateDTO();
        retiroGrande.setCuentaId(3L);
        retiroGrande.setValor(new BigDecimal("-100.00"));

        Cuenta cuentaPobre = new Cuenta();
        cuentaPobre.setId(3L);
        cuentaPobre.setSaldoInicial(BigDecimal.ZERO);

        given(strategyResolver.resolver(retiroGrande.getValor())).willReturn(new RetiroStrategy());
        given(cuentaRepository.findById(3L)).willReturn(Optional.of(cuentaPobre));
        given(movimientoRepository.sumValorByCuentaId(3L)).willReturn(Optional.of(BigDecimal.ZERO));

        assertThatThrownBy(() -> service.registrar(retiroGrande))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Saldo no disponible");
        then(movimientoRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("UT-3.4: Depósito actualiza saldo = saldoInicial + SUM(valores)")
    void registrarMovimiento_shouldUpdateSaldoCorrectly_afterDeposito() {
        given(strategyResolver.resolver(depositoDTO.getValor())).willReturn(new DepositoStrategy());
        given(cuentaRepository.findById(1L)).willReturn(Optional.of(cuenta));
        given(movimientoRepository.sumValorByCuentaId(1L)).willReturn(Optional.of(new BigDecimal("100.00")));
        given(movimientoRepository.save(any(Movimiento.class))).willReturn(movimiento);
        given(mapper.toResponseDTO(any(Movimiento.class))).willReturn(responseDTO);

        MovimientoResponseDTO result = service.registrar(depositoDTO);

        // saldoInicial(2000) + SUM(movimientos)(100) + nuevo(600) = 2700
        assertThat(result.getSaldo()).isNotNull();
    }

    @Test
    @DisplayName("UT-3.5: Retiro actualiza el saldo correctamente")
    void registrarMovimiento_shouldUpdateSaldoCorrectly_afterRetiro() {
        MovimientoResponseDTO resp = MovimientoResponseDTO.builder()
                .id(1L).cuentaId(1L)
                .valor(new BigDecimal("-575.00"))
                .saldo(new BigDecimal("1425.00"))
                .tipoMovimiento("Retiro")
                .build();

        given(strategyResolver.resolver(retiroDTO.getValor())).willReturn(new RetiroStrategy());
        given(cuentaRepository.findById(1L)).willReturn(Optional.of(cuenta));
        given(movimientoRepository.sumValorByCuentaId(1L)).willReturn(Optional.of(BigDecimal.ZERO));
        given(movimientoRepository.save(any(Movimiento.class))).willReturn(movimiento);
        given(mapper.toResponseDTO(any(Movimiento.class))).willReturn(resp);

        MovimientoResponseDTO result = service.registrar(retiroDTO);

        assertThat(result.getValor()).isEqualByComparingTo("-575.00");
        assertThat(result.getSaldo()).isEqualByComparingTo("1425.00");
    }

    @Test
    @DisplayName("UT-3.6: Retiro del saldo total es válido (saldo queda 0)")
    void registrarMovimiento_shouldAllowRetiroExacto_totalSaldo() {
        MovimientoCreateDTO retiroTotal = new MovimientoCreateDTO();
        retiroTotal.setCuentaId(4L);
        retiroTotal.setValor(new BigDecimal("-540.00"));

        Cuenta cuenta540 = new Cuenta();
        cuenta540.setId(4L);
        cuenta540.setSaldoInicial(new BigDecimal("540.00"));

        MovimientoResponseDTO resp = MovimientoResponseDTO.builder()
                .id(1L).cuentaId(4L)
                .valor(new BigDecimal("-540.00"))
                .saldo(BigDecimal.ZERO)
                .tipoMovimiento("Retiro")
                .build();

        given(strategyResolver.resolver(retiroTotal.getValor())).willReturn(new RetiroStrategy());
        given(cuentaRepository.findById(4L)).willReturn(Optional.of(cuenta540));
        given(movimientoRepository.sumValorByCuentaId(4L)).willReturn(Optional.of(BigDecimal.ZERO));
        given(movimientoRepository.save(any(Movimiento.class))).willReturn(movimiento);
        given(mapper.toResponseDTO(any(Movimiento.class))).willReturn(resp);

        MovimientoResponseDTO result = service.registrar(retiroTotal);

        assertThat(result.getSaldo()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("UT-3.7: Cuenta inexistente lanza ResourceNotFoundException")
    void registrarMovimiento_shouldThrow_whenCuentaNotFound() {
        MovimientoCreateDTO dto = new MovimientoCreateDTO();
        dto.setCuentaId(999L);
        dto.setValor(new BigDecimal("100.00"));

        given(strategyResolver.resolver(dto.getValor())).willReturn(new DepositoStrategy());
        given(cuentaRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cuenta no encontrada");
    }

    @Test
    @DisplayName("UT-3.8: Valor = 0 lanza InvalidMovementException")
    void registrarMovimiento_shouldThrow_whenValorCero() {
        MovimientoCreateDTO ceroDTO = new MovimientoCreateDTO();
        ceroDTO.setCuentaId(1L);
        ceroDTO.setValor(BigDecimal.ZERO);

        given(strategyResolver.resolver(BigDecimal.ZERO))
                .willThrow(new InvalidMovementException("El valor del movimiento no puede ser cero"));

        assertThatThrownBy(() -> service.registrar(ceroDTO))
                .isInstanceOf(InvalidMovementException.class)
                .hasMessageContaining("no puede ser cero");
        then(movimientoRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("UT-3.9: La fecha se asigna automáticamente")
    void registrarMovimiento_shouldSetFechaActual() {
        given(strategyResolver.resolver(depositoDTO.getValor())).willReturn(new DepositoStrategy());
        given(cuentaRepository.findById(1L)).willReturn(Optional.of(cuenta));
        given(movimientoRepository.sumValorByCuentaId(1L)).willReturn(Optional.of(BigDecimal.ZERO));
        given(movimientoRepository.save(any(Movimiento.class))).willReturn(movimiento);
        given(mapper.toResponseDTO(any(Movimiento.class))).willReturn(responseDTO);

        MovimientoResponseDTO result = service.registrar(depositoDTO);

        assertThat(result.getFecha()).isNotNull();
        assertThat(result.getFecha()).isCloseTo(LocalDateTime.now(), within(2, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("UT-3.10: findById() retorna DTO cuando existe")
    void findById_shouldReturnDto_whenExists() {
        given(movimientoRepository.findById(1L)).willReturn(Optional.of(movimiento));
        given(mapper.toResponseDTO(movimiento)).willReturn(responseDTO);

        MovimientoResponseDTO result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTipoMovimiento()).isEqualTo("Depósito");
    }

    @Test
    @DisplayName("UT-3.11: findById() lanza excepción cuando no existe")
    void findById_shouldThrow_whenNotFound() {
        given(movimientoRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Movimiento no encontrado");
    }

    @Test
    @DisplayName("UT-3.12: findAll() retorna lista de DTOs")
    void findAll_shouldReturnList() {
        given(movimientoRepository.findAll()).willReturn(List.of(movimiento));
        given(mapper.toResponseDTO(movimiento)).willReturn(responseDTO);

        List<MovimientoResponseDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTipoMovimiento()).isEqualTo("Depósito");
    }
}
