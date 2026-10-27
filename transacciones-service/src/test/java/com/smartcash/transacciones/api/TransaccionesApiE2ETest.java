package com.smartcash.transacciones.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcash.transacciones.api.controllers.*;
import com.smartcash.transacciones.api.dto.ActualizarTransaccionRequestDTO;
import com.smartcash.transacciones.api.dto.MetaAhorroRequestDTO;
import com.smartcash.transacciones.api.dto.MovimientoMetaRequestDTO;
import com.smartcash.transacciones.api.dto.PresupuestoRequestDTO;
import com.smartcash.transacciones.api.dto.TransaccionRequestDTO;
import com.smartcash.transacciones.domain.exceptions.MetaAhorroInvalidaException;
import com.smartcash.transacciones.domain.exceptions.TransaccionInvalidaException;
import com.smartcash.transacciones.domain.model.MetaAhorro;
import com.smartcash.transacciones.domain.model.Presupuesto;
import com.smartcash.transacciones.domain.model.Transaccion;
import com.smartcash.transacciones.domain.ports.in.GestionarMetaAhorroUseCase;
import com.smartcash.transacciones.domain.ports.in.GestionarPresupuestoUseCase;
import com.smartcash.transacciones.domain.ports.in.RegistrarTransaccionUseCase;
import com.smartcash.transacciones.infrastructure.security.JwtAuthFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TransaccionesApiE2ETest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RegistrarTransaccionUseCase transaccionUseCase;
    private GestionarPresupuestoUseCase presupuestoUseCase;
    private GestionarMetaAhorroUseCase metaAhorroUseCase;
    private UUID idUsuario;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        transaccionUseCase = mock(RegistrarTransaccionUseCase.class);
        presupuestoUseCase = mock(GestionarPresupuestoUseCase.class);
        metaAhorroUseCase = mock(GestionarMetaAhorroUseCase.class);

        TransaccionController txController = new TransaccionController(transaccionUseCase);
        PresupuestoController presController = new PresupuestoController(presupuestoUseCase);
        MetaAhorroController metaController = new MetaAhorroController(metaAhorroUseCase);
        CategoriaController catController = new CategoriaController();

        mockMvc = MockMvcBuilders.standaloneSetup(txController, presController, metaController, catController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        idUsuario = UUID.randomUUID();
        auth = new UsernamePasswordAuthenticationToken(idUsuario.toString(), null, Collections.emptyList());
    }

    @Test
    void e2eListarCategoriasOficialesRetorna200() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$[0].nombre").value("Restaurantes y Comida"));
    }

    @Test
    void e2eRegistrarTransaccionExitosoRetorna201() throws Exception {
        UUID idTx = UUID.randomUUID();
        UUID idCat = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Transaccion tx = new Transaccion(idTx, idUsuario, idCat, new BigDecimal("45000"), LocalDate.now(), "Crepes & Waffles", "gasto", 0.95);

        when(transaccionUseCase.registrar(eq(idUsuario), any(), any(), eq("Crepes & Waffles"), eq("gasto")))
                .thenReturn(tx);

        TransaccionRequestDTO req = new TransaccionRequestDTO(idUsuario, new BigDecimal("45000"), LocalDate.now(), "Crepes & Waffles", "gasto");

        mockMvc.perform(post("/transacciones")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTransaccion").value(idTx.toString()))
                .andExpect(jsonPath("$.comercio").value("Crepes & Waffles"));
    }

    @Test
    void e2eListarTransaccionesConFiltrosRetorna200() throws Exception {
        UUID idCat = UUID.fromString("00000000-0000-0000-0000-000000000002");
        Transaccion tx1 = new Transaccion(UUID.randomUUID(), idUsuario, idCat, new BigDecimal("120000"), LocalDate.of(2026, 10, 15), "Exito", "gasto", 0.90);
        Transaccion tx2 = new Transaccion(UUID.randomUUID(), idUsuario, idCat, new BigDecimal("3000000"), LocalDate.of(2026, 10, 1), "Nomina", "ingreso", 0.99);

        when(transaccionUseCase.listarPorUsuario(idUsuario)).thenReturn(List.of(tx1, tx2));

        mockMvc.perform(get("/transacciones")
                        .principal(auth)
                        .param("tipo", "gasto")
                        .param("buscar", "exito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].comercio").value("Exito"));
    }

    @Test
    void e2eActualizarYEliminarTransaccion() throws Exception {
        UUID idTx = UUID.randomUUID();
        UUID idCat = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Transaccion actualizada = new Transaccion(idTx, idUsuario, idCat, new BigDecimal("50000"), LocalDate.now(), "Exito Modificado", "gasto", 1.0);

        when(transaccionUseCase.actualizar(eq(idUsuario), eq(idTx), any(), any(), any(), any(), any()))
                .thenReturn(actualizada);

        ActualizarTransaccionRequestDTO req = new ActualizarTransaccionRequestDTO(
                new BigDecimal("50000"), LocalDate.now(), "Exito Modificado", "gasto", idCat
        );

        mockMvc.perform(put("/transacciones/" + idTx)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comercio").value("Exito Modificado"));

        mockMvc.perform(delete("/transacciones/" + idTx).principal(auth))
                .andExpect(status().isNoContent());
    }

    @Test
    void e2eTransaccionInvalidaRetorna400() throws Exception {
        UUID idTx = UUID.randomUUID();
        when(transaccionUseCase.actualizar(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new TransaccionInvalidaException("Transaccion no encontrada"));

        ActualizarTransaccionRequestDTO req = new ActualizarTransaccionRequestDTO(
                new BigDecimal("50000"), LocalDate.now(), "Comercio", "gasto", null
        );

        mockMvc.perform(put("/transacciones/" + idTx)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Transaccion no encontrada"));
    }

    @Test
    void e2eCrearYListarPresupuesto() throws Exception {
        UUID idCat = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Presupuesto pres = new Presupuesto(UUID.randomUUID(), idUsuario, idCat, "Restaurantes", new BigDecimal("500000"), 10, 2026);

        when(presupuestoUseCase.crearOActualizar(eq(idUsuario), eq(idCat), eq("Restaurantes"), any(), eq(10), eq(2026)))
                .thenReturn(pres);
        when(presupuestoUseCase.listarPorUsuario(idUsuario)).thenReturn(List.of(pres));

        PresupuestoRequestDTO req = new PresupuestoRequestDTO(idCat, "Restaurantes", new BigDecimal("500000"), 10, 2026);

        mockMvc.perform(post("/api/presupuestos")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreCategoria").value("Restaurantes"));

        mockMvc.perform(get("/api/presupuestos").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void e2eCicloCompletoMetasAhorro() throws Exception {
        UUID idMeta = UUID.randomUUID();
        MetaAhorro meta = new MetaAhorro(idMeta, idUsuario, "Vacaciones", "Playa", new BigDecimal("2000000"), new BigDecimal("500000"), LocalDate.now().plusMonths(3), "EN_PROGRESO");

        when(metaAhorroUseCase.crearMeta(eq(idUsuario), eq("Vacaciones"), eq("Playa"), any(), any())).thenReturn(meta);
        when(metaAhorroUseCase.listarPorUsuario(idUsuario)).thenReturn(List.of(meta));
        when(metaAhorroUseCase.abonarMeta(eq(idUsuario), eq(idMeta), any())).thenReturn(meta);
        when(metaAhorroUseCase.retirarMeta(eq(idUsuario), eq(idMeta), any()))
                .thenThrow(new MetaAhorroInvalidaException("Saldo insuficiente"));

        MetaAhorroRequestDTO reqCrear = new MetaAhorroRequestDTO("Vacaciones", "Playa", new BigDecimal("2000000"), LocalDate.now().plusMonths(3));

        mockMvc.perform(post("/api/metas-ahorro")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqCrear)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Vacaciones"));

        mockMvc.perform(get("/api/metas-ahorro").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        MovimientoMetaRequestDTO reqMov = new MovimientoMetaRequestDTO(new BigDecimal("100000"));

        mockMvc.perform(post("/api/metas-ahorro/" + idMeta + "/abonar")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqMov)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/metas-ahorro/" + idMeta + "/retirar")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqMov)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Saldo insuficiente"));

        mockMvc.perform(delete("/api/metas-ahorro/" + idMeta).principal(auth))
                .andExpect(status().isNoContent());
    }

    @Test
    void e2eJwtAuthFilterValidaTokenCorrectamente() throws Exception {
        String secret = "12345678901234567890123456789012_secret_key_32_bytes";
        JwtAuthFilter filter = new JwtAuthFilter(secret);
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject("test@smartcash.com")
                .claim("id_usuario", idUsuario.toString())
                .claim("nombre", "Tester")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(idUsuario.toString(), SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        SecurityContextHolder.clearContext();
    }
}
