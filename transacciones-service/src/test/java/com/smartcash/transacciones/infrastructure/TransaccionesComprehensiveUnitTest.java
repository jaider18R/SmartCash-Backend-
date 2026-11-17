package com.smartcash.transacciones.infrastructure;

import com.smartcash.transacciones.application.services.PresupuestoService;
import com.smartcash.transacciones.application.services.RegistrarTransaccionService;
import com.smartcash.transacciones.domain.exceptions.TransaccionInvalidaException;
import com.smartcash.transacciones.domain.model.MetaAhorro;
import com.smartcash.transacciones.domain.model.Presupuesto;
import com.smartcash.transacciones.domain.model.Transaccion;
import com.smartcash.transacciones.domain.ports.in.VerificarPresupuestoUseCase;
import com.smartcash.transacciones.domain.ports.out.*;
import com.smartcash.transacciones.infrastructure.clients.CategorizacionHttpAdapter;
import com.smartcash.transacciones.infrastructure.clients.CategorizacionMockAdapter;
import com.smartcash.transacciones.infrastructure.clients.NotificacionRestClientAdapter;
import com.smartcash.transacciones.infrastructure.persistence.*;
import com.smartcash.transacciones.infrastructure.security.JwtAuthFilter;
import com.smartcash.transacciones.infrastructure.security.SecurityConfig;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransaccionesComprehensiveUnitTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deberiaCubrirTransaccionRepositoryAdapter() {
        TransaccionJpaRepository jpaRepo = mock(TransaccionJpaRepository.class);
        TransaccionRepositoryAdapter adapter = new TransaccionRepositoryAdapter(jpaRepo);

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        LocalDate hoy = LocalDate.now();
        Transaccion domain = new Transaccion(id, userId, catId, new BigDecimal("50000"), hoy, "Exito", "gasto", 0.95);
        TransaccionJpaEntity entity = new TransaccionJpaEntity(id, userId, catId, new BigDecimal("50000"), hoy, "Exito", "gasto", 0.95);

        when(jpaRepo.save(any(TransaccionJpaEntity.class))).thenReturn(entity);
        when(jpaRepo.findByIdUsuario(userId)).thenReturn(List.of(entity));
        when(jpaRepo.findById(id)).thenReturn(Optional.of(entity));
        when(jpaRepo.sumMontoGastosPorUsuarioCategoriaYPeriodo(userId, catId, hoy, hoy)).thenReturn(new BigDecimal("50000"));

        Transaccion guardada = adapter.guardar(domain);
        assertEquals(id, guardada.getId());
        assertEquals(1, adapter.listarPorUsuario(userId).size());
        assertTrue(adapter.buscarPorId(id).isPresent());
        assertEquals(new BigDecimal("50000"), adapter.calcularGastoAcumulado(userId, catId, hoy, hoy));
        adapter.eliminar(id);
        verify(jpaRepo).deleteById(id);
    }

    @Test
    void deberiaCubrirMetaAhorroRepositoryAdapter() {
        MetaAhorroJpaRepository jpaRepo = mock(MetaAhorroJpaRepository.class);
        MetaAhorroRepositoryAdapter adapter = new MetaAhorroRepositoryAdapter(jpaRepo);

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate limite = LocalDate.now().plusMonths(3);
        MetaAhorro domain = new MetaAhorro(id, userId, "Viaje", "Vacaciones", new BigDecimal("1000000"), new BigDecimal("200000"), limite, "EN_PROGRESO");
        MetaAhorroJpaEntity entity = new MetaAhorroJpaEntity(id, userId, "Viaje", "Vacaciones", new BigDecimal("1000000"), new BigDecimal("200000"), limite, "EN_PROGRESO");

        when(jpaRepo.save(any(MetaAhorroJpaEntity.class))).thenReturn(entity);
        when(jpaRepo.findById(id)).thenReturn(Optional.of(entity));
        when(jpaRepo.findByIdUsuario(userId)).thenReturn(List.of(entity));

        MetaAhorro guardada = adapter.guardar(domain);
        assertEquals(id, guardada.getId());
        assertTrue(adapter.buscarPorId(id).isPresent());
        assertEquals(1, adapter.listarPorUsuario(userId).size());
        adapter.eliminar(id);
        verify(jpaRepo).deleteById(id);
    }

    @Test
    void deberiaCubrirPresupuestoRepositoryAdapter() {
        PresupuestoJpaRepository jpaRepo = mock(PresupuestoJpaRepository.class);
        PresupuestoRepositoryAdapter adapter = new PresupuestoRepositoryAdapter(jpaRepo);

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Presupuesto domain = new Presupuesto(id, userId, catId, "Mercado", new BigDecimal("500000"), 10, 2026);
        PresupuestoJpaEntity entity = new PresupuestoJpaEntity(id, userId, catId, "Mercado", new BigDecimal("500000"), 10, 2026);

        when(jpaRepo.save(any(PresupuestoJpaEntity.class))).thenReturn(entity);
        when(jpaRepo.findByIdUsuario(userId)).thenReturn(List.of(entity));
        when(jpaRepo.findByIdUsuarioAndIdCategoriaAndMesAndAnio(userId, catId, 10, 2026)).thenReturn(Optional.of(entity));

        Presupuesto guardado = adapter.guardar(domain);
        assertEquals(id, guardado.getId());
        assertEquals(1, adapter.listarPorUsuario(userId).size());
        assertTrue(adapter.buscarPorUsuarioYCategoriaYPeriodo(userId, catId, 10, 2026).isPresent());
    }

    @Test
    void deberiaCubrirCategorizacionMockAdapter() {
        CategorizacionMockAdapter mockAdapter = new CategorizacionMockAdapter();
        var r1 = mockAdapter.clasificarConDetalle(UUID.randomUUID(), "Rappi Burger", new BigDecimal("35000"));
        assertNotNull(r1.idCategoria());
        assertEquals(0.75, r1.confianza());

        var r2 = mockAdapter.clasificar("Tienda Desconocida", new BigDecimal("10000"));
        assertNull(r2.idCategoria());
        assertEquals(0.0, r2.confianza());

        assertNotNull(mockAdapter.obtenerExplicacion(1L));
    }

    @Test
    void deberiaCubrirCategorizacionHttpAdapter() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/clasificar", exchange -> {
            String path = exchange.getRequestURI().getPath();
            byte[] resp;
            if (path.endsWith("/explicacion")) {
                resp = "{\"explicacion\":\"Explicacion IA\"}".getBytes(StandardCharsets.UTF_8);
            } else {
                resp = "{\"categoria\":\"Restaurantes y Comida\",\"nivel_confianza\":0.92}".getBytes(StandardCharsets.UTF_8);
            }
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });
        server.createContext("/patrones/analizar", exchange -> {
            byte[] resp = "{\"es_inusual\":true,\"descripcion_alerta\":\"Monto superior al promedio\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort();
            CategorizacionHttpAdapter adapter = new CategorizacionHttpAdapter(url);

            var det = adapter.clasificarConDetalle(UUID.randomUUID(), "Crepes", new BigDecimal("80000"));
            assertNotNull(det.idCategoria());
            assertEquals(0.92, det.confianza(), 0.01);
            assertTrue(det.esInusual());
            assertEquals("Monto superior al promedio", det.alertaInusual());

            var simple = adapter.clasificar("Crepes", new BigDecimal("80000"));
            assertNotNull(simple.idCategoria());

            String exp = adapter.obtenerExplicacion(100L);
            assertEquals("Explicacion IA", exp);
        } finally {
            server.stop(0);
        }

        CategorizacionHttpAdapter adapterFallo = new CategorizacionHttpAdapter("http://localhost:1");
        var fallo = adapterFallo.clasificarConDetalle(UUID.randomUUID(), "Crepes", new BigDecimal("80000"));
        assertNull(fallo.idCategoria());
        assertNotNull(adapterFallo.obtenerExplicacion(1L));
    }

    @Test
    void deberiaCubrirNotificacionRestClientAdapter() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/notificaciones/email", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort();
            NotificacionRestClientAdapter adapter = new NotificacionRestClientAdapter(url);

            adapter.enviarAlertaPresupuesto(null, "Juan", "Mercado", new BigDecimal("85"));
            adapter.enviarAlertaPresupuesto("  ", "Juan", "Mercado", new BigDecimal("85"));
            adapter.enviarAlertaPresupuesto("juan@test.com", "Juan", "Mercado", new BigDecimal("85.50"));
            adapter.enviarAlertaPresupuesto("juan@test.com", null, "Mercado", new BigDecimal("90"));

            adapter.enviarAlertaGastoInusual(null, "Juan", "Rappi", new BigDecimal("500000"), "Inusual");
            adapter.enviarAlertaGastoInusual("  ", "Juan", "Rappi", new BigDecimal("500000"), "Inusual");
            adapter.enviarAlertaGastoInusual("juan@test.com", "Juan", "Rappi", new BigDecimal("500000"), "Inusual");
            adapter.enviarAlertaGastoInusual("juan@test.com", "", "Rappi", new BigDecimal("500000"), null);
        } finally {
            server.stop(0);
        }

        NotificacionRestClientAdapter adapterFallo = new NotificacionRestClientAdapter("http://localhost:1");
        assertDoesNotThrow(() -> adapterFallo.enviarAlertaPresupuesto("juan@test.com", "Juan", "Mercado", new BigDecimal("95")));
        assertDoesNotThrow(() -> adapterFallo.enviarAlertaGastoInusual("juan@test.com", "Juan", "Rappi", new BigDecimal("500000"), "Inusual"));
    }

    @Test
    void deberiaCubrirRegistrarTransaccionServiceCompleto() {
        RepositorioTransaccionPort repo = mock(RepositorioTransaccionPort.class);
        ClasificadorTransaccionPort clasificador = mock(ClasificadorTransaccionPort.class);
        VerificarPresupuestoUseCase verificarPresupuesto = mock(VerificarPresupuestoUseCase.class);
        AlertaNotificacionPort alertaNotificacion = mock(AlertaNotificacionPort.class);

        RegistrarTransaccionService service = new RegistrarTransaccionService(repo, clasificador, verificarPresupuesto, alertaNotificacion);

        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();
        LocalDate hoy = LocalDate.now();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of());
        auth.setDetails(Map.of("email", "user@smartcash.com", "nombre", "Carlos"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(clasificador.clasificarConDetalle(eq(userId), anyString(), any()))
                .thenReturn(new ClasificadorTransaccionPort.ResultadoClasificacionDetallada(catId, 0.9, 1L, true, "Alerta IA"));
        when(repo.guardar(any(Transaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaccion creada = service.registrar(userId, new BigDecimal("120000"), hoy, "Tienda Cara", "gasto");
        assertEquals(catId, creada.getIdCategoria());
        verify(alertaNotificacion).enviarAlertaGastoInusual(eq("user@smartcash.com"), eq("Carlos"), eq("Tienda Cara"), any(), eq("Alerta IA"));
        verify(verificarPresupuesto).verificarPresupuesto(eq(userId), eq(catId), eq(hoy), any());

        UsernamePasswordAuthenticationToken authString = new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of());
        authString.setDetails("otro@smartcash.com");
        SecurityContextHolder.getContext().setAuthentication(authString);
        service.registrar(userId, new BigDecimal("50000"), hoy, "Tienda 2", "gasto");

        Transaccion existente = new Transaccion(txId, userId, catId, new BigDecimal("10000"), hoy, "Viejo", "gasto", 0.8);
        when(repo.buscarPorId(txId)).thenReturn(Optional.of(existente));

        Transaccion actualizada = service.actualizar(userId, txId, new BigDecimal("25000"), hoy, "Nuevo", "gasto", null);
        assertEquals("Nuevo", actualizada.getComercio());

        assertThrows(TransaccionInvalidaException.class, () ->
                service.actualizar(UUID.randomUUID(), txId, new BigDecimal("25000"), hoy, "Nuevo", "gasto", catId));

        assertThrows(TransaccionInvalidaException.class, () ->
                service.eliminar(UUID.randomUUID(), txId));

        service.eliminar(userId, txId);
        verify(repo).eliminar(txId);

        assertThrows(TransaccionInvalidaException.class, () ->
                service.registrar(userId, BigDecimal.ZERO, hoy, "Tienda", "gasto"));
        assertThrows(TransaccionInvalidaException.class, () ->
                service.registrar(userId, new BigDecimal("10"), hoy, "  ", "gasto"));
        assertThrows(TransaccionInvalidaException.class, () ->
                service.registrar(userId, new BigDecimal("10"), hoy, "Tienda", "invalido"));
    }

    @Test
    void deberiaCubrirPresupuestoServiceRamasAdicionales() {
        RepositorioPresupuestoPort repoPres = mock(RepositorioPresupuestoPort.class);
        RepositorioTransaccionPort repoTx = mock(RepositorioTransaccionPort.class);
        AlertaNotificacionPort alertaPort = mock(AlertaNotificacionPort.class);

        PresupuestoService service = new PresupuestoService(repoPres, repoTx, alertaPort);
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();

        service.verificarPresupuesto(null, catId, LocalDate.now(), BigDecimal.TEN);
        service.verificarPresupuesto(userId, null, LocalDate.now(), BigDecimal.TEN);
        service.verificarPresupuesto(userId, catId, null, BigDecimal.TEN);

        Presupuesto existente = new Presupuesto(UUID.randomUUID(), userId, catId, "Mercado", new BigDecimal("200000"), 10, 2026);
        when(repoPres.buscarPorUsuarioYCategoriaYPeriodo(userId, catId, 10, 2026)).thenReturn(Optional.of(existente));
        when(repoPres.guardar(any())).thenReturn(existente);
        when(repoTx.calcularGastoAcumulado(any(), any(), any(), any())).thenReturn(new BigDecimal("180000"));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of());
        auth.setDetails(Map.of("email", "ana@smartcash.com", "nombre", "Ana"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Presupuesto actualizado = service.crearOActualizar(userId, catId, "Mercado", new BigDecimal("200000"), 10, 2026);
        assertNotNull(actualizado);
        verify(alertaPort).enviarAlertaPresupuesto(eq("ana@smartcash.com"), eq("Ana"), eq("Mercado"), any());

        SecurityConfig secConfig = new SecurityConfig(mock(JwtAuthFilter.class));
        CorsConfigurationSource corsSource = secConfig.corsConfigurationSource();
        CorsConfiguration cors = corsSource.getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(cors);
    }
}
