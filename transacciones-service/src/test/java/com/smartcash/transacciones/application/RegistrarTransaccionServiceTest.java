package com.smartcash.transacciones.application;

import com.smartcash.transacciones.application.services.RegistrarTransaccionService;
import com.smartcash.transacciones.domain.exceptions.TransaccionInvalidaException;
import com.smartcash.transacciones.domain.model.Transaccion;
import com.smartcash.transacciones.domain.ports.out.ClasificadorTransaccionPort;
import com.smartcash.transacciones.domain.ports.out.RepositorioTransaccionPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Esta es la ventaja practica de la arquitectura hexagonal + DIP:
 * probamos la REGLA DE NEGOCIO sin levantar Spring, sin base de datos,
 * sin HTTP -- solo con "dobles" (mocks) de los puertos.
 */
class RegistrarTransaccionServiceTest {

    @Test
    void deberiaRechazarMontoNegativoOCero() {
        RepositorioTransaccionPort repoMock = mock(RepositorioTransaccionPort.class);
        ClasificadorTransaccionPort clasificadorMock = mock(ClasificadorTransaccionPort.class);
        RegistrarTransaccionService service = new RegistrarTransaccionService(repoMock, clasificadorMock);

        assertThrows(TransaccionInvalidaException.class, () ->
                service.registrar(UUID.randomUUID(), BigDecimal.ZERO, LocalDate.now(), "Rappi", "gasto"));
    }

    @Test
    void deberiaRegistrarYClasificarUnaTransaccionValida() {
        RepositorioTransaccionPort repoMock = mock(RepositorioTransaccionPort.class);
        ClasificadorTransaccionPort clasificadorMock = mock(ClasificadorTransaccionPort.class);

        UUID idCategoria = UUID.randomUUID();
        when(clasificadorMock.clasificar(eq("Rappi"), any()))
                .thenReturn(new ClasificadorTransaccionPort.ResultadoClasificacion(idCategoria, 0.9));
        when(repoMock.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistrarTransaccionService service = new RegistrarTransaccionService(repoMock, clasificadorMock);

        Transaccion resultado = service.registrar(
                UUID.randomUUID(), new BigDecimal("25000"), LocalDate.now(), "Rappi", "gasto");

        assertEquals(idCategoria, resultado.getIdCategoria());
        assertEquals(0.9, resultado.getConfianzaCategorizacion());
        verify(repoMock, times(1)).guardar(any());
    }
}
