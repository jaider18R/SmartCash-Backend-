package com.smartcash.transacciones.application;

import com.smartcash.transacciones.application.services.PresupuestoService;
import com.smartcash.transacciones.domain.model.Presupuesto;
import com.smartcash.transacciones.domain.ports.out.AlertaNotificacionPort;
import com.smartcash.transacciones.domain.ports.out.RepositorioPresupuestoPort;
import com.smartcash.transacciones.domain.ports.out.RepositorioTransaccionPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PresupuestoServiceTest {

    private RepositorioPresupuestoPort repositorioPresupuestoMock;
    private RepositorioTransaccionPort repositorioTransaccionMock;
    private AlertaNotificacionPort alertaNotificacionMock;
    private PresupuestoService service;

    @BeforeEach
    void setUp() {
        repositorioPresupuestoMock = mock(RepositorioPresupuestoPort.class);
        repositorioTransaccionMock = mock(RepositorioTransaccionPort.class);
        alertaNotificacionMock = mock(AlertaNotificacionPort.class);
        service = new PresupuestoService(repositorioPresupuestoMock, repositorioTransaccionMock, alertaNotificacionMock);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user-123", null);
        auth.setDetails("usuario@test.com");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deberiaCrearPresupuestoCorrectamente() {
        UUID idUsuario = UUID.randomUUID();
        UUID idCategoria = UUID.randomUUID();
        when(repositorioPresupuestoMock.buscarPorUsuarioYCategoriaYPeriodo(idUsuario, idCategoria, 10, 2026))
                .thenReturn(Optional.empty());
        when(repositorioPresupuestoMock.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Presupuesto creado = service.crearOActualizar(idUsuario, idCategoria, "Restaurantes", new BigDecimal("500000"), 10, 2026);

        assertEquals("Restaurantes", creado.getNombreCategoria());
        assertEquals(new BigDecimal("500000"), creado.getMontoLimite());
        verify(repositorioPresupuestoMock, times(1)).guardar(any());
    }

    @Test
    void deberiaEnviarAlertaCuandoSeSuperaElUmbralDelOchentaPorciento() {
        UUID idUsuario = UUID.randomUUID();
        UUID idCategoria = UUID.randomUUID();
        Presupuesto presupuesto = Presupuesto.nuevo(idUsuario, idCategoria, "Restaurantes", new BigDecimal("100000"), 10, 2026);

        when(repositorioPresupuestoMock.buscarPorUsuarioYCategoriaYPeriodo(eq(idUsuario), eq(idCategoria), eq(10), eq(2026)))
                .thenReturn(Optional.of(presupuesto));
        when(repositorioTransaccionMock.calcularGastoAcumulado(eq(idUsuario), eq(idCategoria), any(), any()))
                .thenReturn(new BigDecimal("85000"));

        service.verificarPresupuesto(idUsuario, idCategoria, LocalDate.of(2026, 10, 9), new BigDecimal("15000"));

        verify(alertaNotificacionMock, times(1)).enviarAlertaPresupuesto(
                eq("usuario@test.com"),
                eq("Restaurantes"),
                eq(new BigDecimal("85.00"))
        );
    }

    @Test
    void noDeberiaEnviarAlertaCuandoGastoEsMenorAlOchentaPorciento() {
        UUID idUsuario = UUID.randomUUID();
        UUID idCategoria = UUID.randomUUID();
        Presupuesto presupuesto = Presupuesto.nuevo(idUsuario, idCategoria, "Restaurantes", new BigDecimal("100000"), 10, 2026);

        when(repositorioPresupuestoMock.buscarPorUsuarioYCategoriaYPeriodo(eq(idUsuario), eq(idCategoria), eq(10), eq(2026)))
                .thenReturn(Optional.of(presupuesto));
        when(repositorioTransaccionMock.calcularGastoAcumulado(eq(idUsuario), eq(idCategoria), any(), any()))
                .thenReturn(new BigDecimal("50000"));

        service.verificarPresupuesto(idUsuario, idCategoria, LocalDate.of(2026, 10, 9), new BigDecimal("10000"));

        verify(alertaNotificacionMock, never()).enviarAlertaPresupuesto(any(), any(), any());
    }
}
