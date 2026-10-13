package com.smartcash.transacciones.application;

import com.smartcash.transacciones.application.services.MetaAhorroService;
import com.smartcash.transacciones.domain.exceptions.MetaAhorroInvalidaException;
import com.smartcash.transacciones.domain.model.MetaAhorro;
import com.smartcash.transacciones.domain.ports.out.RepositorioMetaAhorroPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetaAhorroServiceTest {

    private RepositorioMetaAhorroPort repositorioMock;
    private MetaAhorroService service;

    @BeforeEach
    void setUp() {
        repositorioMock = mock(RepositorioMetaAhorroPort.class);
        service = new MetaAhorroService(repositorioMock);
    }

    @Test
    void deberiaCrearMetaAhorroValida() {
        UUID idUsuario = UUID.randomUUID();
        when(repositorioMock.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        MetaAhorro meta = service.crearMeta(
                idUsuario,
                "Viaje fin de año",
                "Ahorro para vacaciones",
                new BigDecimal("2000000"),
                LocalDate.now().plusMonths(3)
        );

        assertNotNull(meta);
        assertEquals("Viaje fin de año", meta.getNombre());
        assertEquals(new BigDecimal("2000000"), meta.getMontoObjetivo());
        assertEquals(BigDecimal.ZERO, meta.getMontoActual());
        assertEquals("EN_PROGRESO", meta.getEstado());
        verify(repositorioMock, times(1)).guardar(any());
    }

    @Test
    void deberiaRechazarCrearMetaConMontoInvalido() {
        UUID idUsuario = UUID.randomUUID();

        assertThrows(MetaAhorroInvalidaException.class, () ->
                service.crearMeta(idUsuario, "Carro", "Meta", BigDecimal.ZERO, LocalDate.now().plusDays(10)));
    }

    @Test
    void deberiaAbonarDineroAMetaYCompletarlaSiAlcanzaElObjetivo() {
        UUID idUsuario = UUID.randomUUID();
        UUID idMeta = UUID.randomUUID();
        MetaAhorro metaExistente = new MetaAhorro(
                idMeta, idUsuario, "Viaje", "Desc",
                new BigDecimal("1000000"), new BigDecimal("800000"),
                LocalDate.now().plusMonths(1), "EN_PROGRESO"
        );

        when(repositorioMock.buscarPorId(idMeta)).thenReturn(Optional.of(metaExistente));
        when(repositorioMock.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        MetaAhorro actualizada = service.abonarMeta(idUsuario, idMeta, new BigDecimal("300000"));

        assertEquals(new BigDecimal("1100000"), actualizada.getMontoActual());
        assertEquals("COMPLETADA", actualizada.getEstado());
        verify(repositorioMock, times(1)).guardar(any());
    }

    @Test
    void deberiaRetirarDineroDeMeta() {
        UUID idUsuario = UUID.randomUUID();
        UUID idMeta = UUID.randomUUID();
        MetaAhorro metaExistente = new MetaAhorro(
                idMeta, idUsuario, "Viaje", "Desc",
                new BigDecimal("1000000"), new BigDecimal("500000"),
                LocalDate.now().plusMonths(1), "EN_PROGRESO"
        );

        when(repositorioMock.buscarPorId(idMeta)).thenReturn(Optional.of(metaExistente));
        when(repositorioMock.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        MetaAhorro actualizada = service.retirarMeta(idUsuario, idMeta, new BigDecimal("200000"));

        assertEquals(new BigDecimal("300000"), actualizada.getMontoActual());
        assertEquals("EN_PROGRESO", actualizada.getEstado());
    }

    @Test
    void deberiaRechazarRetiroSiSuperaSaldoActual() {
        UUID idUsuario = UUID.randomUUID();
        UUID idMeta = UUID.randomUUID();
        MetaAhorro metaExistente = new MetaAhorro(
                idMeta, idUsuario, "Viaje", "Desc",
                new BigDecimal("1000000"), new BigDecimal("100000"),
                LocalDate.now().plusMonths(1), "EN_PROGRESO"
        );

        when(repositorioMock.buscarPorId(idMeta)).thenReturn(Optional.of(metaExistente));

        assertThrows(MetaAhorroInvalidaException.class, () ->
                service.retirarMeta(idUsuario, idMeta, new BigDecimal("150000")));
    }
}
