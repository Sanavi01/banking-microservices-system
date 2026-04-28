package com.sofka.ms_cuentas_movimientos.service;

import com.sofka.ms_cuentas_movimientos.dto.*;
import com.sofka.ms_cuentas_movimientos.entity.Cuenta;
import com.sofka.ms_cuentas_movimientos.exception.DuplicateResourceException;
import com.sofka.ms_cuentas_movimientos.exception.ResourceNotFoundException;
import com.sofka.ms_cuentas_movimientos.mapper.CuentaMapper;
import com.sofka.ms_cuentas_movimientos.repository.ClienteRepository;
import com.sofka.ms_cuentas_movimientos.repository.CuentaRepository;
import com.sofka.ms_cuentas_movimientos.repository.MovimientoRepository;
import com.sofka.ms_cuentas_movimientos.service.impl.CuentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CuentaService Unit Tests")
class CuentaServiceTest {

    @Mock private CuentaRepository cuentaRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private MovimientoRepository movimientoRepository;
    @Mock private CuentaMapper mapper;

    @InjectMocks
    private CuentaServiceImpl service;

    private CuentaCreateDTO createDTO;
    private Cuenta cuenta;
    private CuentaResponseDTO responseDTO;
    private static final String CLIENTE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final BigDecimal SALDO = new BigDecimal("2000.00");

    @BeforeEach
    void setUp() {
        createDTO = new CuentaCreateDTO();
        createDTO.setNumeroCuenta("478758");
        createDTO.setTipoCuenta("Ahorro");
        createDTO.setSaldoInicial(SALDO);
        createDTO.setEstado(true);
        createDTO.setClienteId(CLIENTE_ID);

        cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setNumeroCuenta("478758");
        cuenta.setTipoCuenta("Ahorro");
        cuenta.setSaldoInicial(SALDO);
        cuenta.setEstado(true);
        cuenta.setClienteId(CLIENTE_ID);

        responseDTO = CuentaResponseDTO.builder()
                .id(1L)
                .numeroCuenta("478758")
                .tipoCuenta("Ahorro")
                .saldoInicial(SALDO)
                .saldoActual(SALDO)
                .estado(true)
                .clienteId(CLIENTE_ID)
                .build();
    }

    @Test
    @DisplayName("UT-2.1: create() con datos válidos retorna DTO")
    void create_shouldReturnDto_whenValidData() {
        given(clienteRepository.existsById(CLIENTE_ID)).willReturn(true);
        given(cuentaRepository.existsByNumeroCuenta("478758")).willReturn(false);
        given(mapper.toEntity(createDTO)).willReturn(cuenta);
        given(cuentaRepository.save(cuenta)).willReturn(cuenta);
        given(mapper.toResponseDTO(eq(cuenta), any(BigDecimal.class))).willReturn(responseDTO);

        CuentaResponseDTO result = service.create(createDTO);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNumeroCuenta()).isEqualTo("478758");
        assertThat(result.getTipoCuenta()).isEqualTo("Ahorro");
    }

    @Test
    @DisplayName("UT-2.2: create() lanza excepción cuando cliente no existe")
    void create_shouldThrow_whenClienteNotFound() {
        given(clienteRepository.existsById("XYZ-999")).willReturn(false);
        createDTO.setClienteId("XYZ-999");

        assertThatThrownBy(() -> service.create(createDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado");
        then(cuentaRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("UT-2.3: create() lanza excepción cuando numeroCuenta duplicado")
    void create_shouldThrow_whenNumeroCuentaDuplicado() {
        given(clienteRepository.existsById(CLIENTE_ID)).willReturn(true);
        given(cuentaRepository.existsByNumeroCuenta("478758")).willReturn(true);

        assertThatThrownBy(() -> service.create(createDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("478758");
        then(cuentaRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("UT-2.4: findById() retorna DTO cuando existe")
    void findById_shouldReturnDto_whenExists() {
        given(cuentaRepository.findById(1L)).willReturn(Optional.of(cuenta));
        given(movimientoRepository.sumValorByCuentaId(1L)).willReturn(Optional.of(BigDecimal.ZERO));
        given(mapper.toResponseDTO(eq(cuenta), any(BigDecimal.class))).willReturn(responseDTO);

        CuentaResponseDTO result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNumeroCuenta()).isEqualTo("478758");
    }

    @Test
    @DisplayName("UT-2.5: findById() lanza excepción cuando no existe")
    void findById_shouldThrow_whenNotFound() {
        given(cuentaRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cuenta no encontrada");
    }

    @Test
    @DisplayName("UT-2.6: findAll() retorna lista de DTOs")
    void findAll_shouldReturnList() {
        given(cuentaRepository.findAll()).willReturn(List.of(cuenta));
        given(movimientoRepository.sumValorByCuentaId(1L)).willReturn(Optional.of(BigDecimal.ZERO));
        given(mapper.toResponseDTO(eq(cuenta), any(BigDecimal.class))).willReturn(responseDTO);

        List<CuentaResponseDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNumeroCuenta()).isEqualTo("478758");
    }

    @Test
    @DisplayName("UT-2.7: update() actualiza todos los campos")
    void update_shouldUpdateAllFields() {
        CuentaUpdateDTO updateDTO = new CuentaUpdateDTO();
        updateDTO.setTipoCuenta("Corriente");
        updateDTO.setSaldoInicial(new BigDecimal("3000.00"));

        given(cuentaRepository.findById(1L)).willReturn(Optional.of(cuenta));
        given(movimientoRepository.sumValorByCuentaId(1L)).willReturn(Optional.of(BigDecimal.ZERO));
        given(cuentaRepository.save(cuenta)).willReturn(cuenta);
        given(mapper.toResponseDTO(eq(cuenta), any(BigDecimal.class))).willReturn(responseDTO);

        CuentaResponseDTO result = service.update(1L, updateDTO);

        assertThat(result.getId()).isEqualTo(1L);
        then(mapper).should().updateEntity(cuenta, updateDTO);
    }

    @Test
    @DisplayName("UT-2.8: patch() actualiza solo los campos enviados")
    void patch_shouldUpdateOnlyProvidedFields() {
        CuentaPatchDTO patchDTO = new CuentaPatchDTO();
        patchDTO.setEstado(false);

        given(cuentaRepository.findById(1L)).willReturn(Optional.of(cuenta));
        given(movimientoRepository.sumValorByCuentaId(1L)).willReturn(Optional.of(BigDecimal.ZERO));
        given(cuentaRepository.save(cuenta)).willReturn(cuenta);
        given(mapper.toResponseDTO(eq(cuenta), any(BigDecimal.class))).willReturn(responseDTO);

        CuentaResponseDTO result = service.patch(1L, patchDTO);

        assertThat(result.getId()).isEqualTo(1L);
        then(mapper).should().patchEntity(cuenta, patchDTO);
    }

    @Test
    @DisplayName("UT-2.9: delete() elimina la cuenta")
    void delete_shouldRemoveCuenta() {
        given(cuentaRepository.findById(1L)).willReturn(Optional.of(cuenta));

        service.delete(1L);

        then(cuentaRepository).should().delete(cuenta);
    }
}
