package com.smartcash.transacciones.application.services;

import com.smartcash.transacciones.domain.exceptions.TransaccionInvalidaException;
import com.smartcash.transacciones.domain.model.Transaccion;
import com.smartcash.transacciones.domain.ports.in.RegistrarTransaccionUseCase;
import com.smartcash.transacciones.domain.ports.out.ClasificadorTransaccionPort;
import com.smartcash.transacciones.domain.ports.out.RepositorioTransaccionPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * SRP: coordina el caso de uso "registrar transaccion" -- validar, clasificar, guardar.
 * DIP: depende de dos puertos (RepositorioTransaccionPort, ClasificadorTransaccionPort),
 * ninguno de los dos una clase concreta. No importa si el clasificador es hoy un mock
 * y manana una llamada real a Python: este servicio no cambia una linea.
 */
@Service
public class RegistrarTransaccionService implements RegistrarTransaccionUseCase {

    private final RepositorioTransaccionPort repositorioTransaccion;
    private final ClasificadorTransaccionPort clasificador;

    public RegistrarTransaccionService(RepositorioTransaccionPort repositorioTransaccion,
                                        ClasificadorTransaccionPort clasificador) {
        this.repositorioTransaccion = repositorioTransaccion;
        this.clasificador = clasificador;
    }

    @Override
    public Transaccion registrar(UUID idUsuario, BigDecimal monto, LocalDate fecha,
                                  String comercio, String tipoMovimiento) {
        validar(monto, comercio, tipoMovimiento);

        Transaccion transaccion = Transaccion.nueva(idUsuario, monto, fecha, comercio, tipoMovimiento);

        ClasificadorTransaccionPort.ResultadoClasificacion resultado =
                clasificador.clasificar(comercio, monto);

        Transaccion transaccionCategorizada = transaccion.conCategoria(
                resultado.idCategoria(), resultado.confianza());

        return repositorioTransaccion.guardar(transaccionCategorizada);
    }

    private void validar(BigDecimal monto, String comercio, String tipoMovimiento) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransaccionInvalidaException("El monto debe ser mayor que cero");
        }
        if (comercio == null || comercio.isBlank()) {
            throw new TransaccionInvalidaException("El comercio es obligatorio");
        }
        if (!tipoMovimiento.equals("ingreso") && !tipoMovimiento.equals("gasto")) {
            throw new TransaccionInvalidaException("tipo_movimiento debe ser 'ingreso' o 'gasto'");
        }
    }
}
