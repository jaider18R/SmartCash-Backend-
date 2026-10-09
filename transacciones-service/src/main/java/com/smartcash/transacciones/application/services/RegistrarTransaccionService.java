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

@Service
public class RegistrarTransaccionService implements RegistrarTransaccionUseCase {

    private final RepositorioTransaccionPort repositorioTransaccion;
    private final ClasificadorTransaccionPort clasificador;
    private final com.smartcash.transacciones.domain.ports.in.VerificarPresupuestoUseCase verificarPresupuestoUseCase;

    public RegistrarTransaccionService(RepositorioTransaccionPort repositorioTransaccion,
                                        ClasificadorTransaccionPort clasificador,
                                        com.smartcash.transacciones.domain.ports.in.VerificarPresupuestoUseCase verificarPresupuestoUseCase) {
        this.repositorioTransaccion = repositorioTransaccion;
        this.clasificador = clasificador;
        this.verificarPresupuestoUseCase = verificarPresupuestoUseCase;
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

        Transaccion guardada = repositorioTransaccion.guardar(transaccionCategorizada);

        if ("gasto".equalsIgnoreCase(tipoMovimiento) && guardada.getIdCategoria() != null) {
            verificarPresupuestoUseCase.verificarPresupuesto(
                    guardada.getIdUsuario(),
                    guardada.getIdCategoria(),
                    guardada.getFecha(),
                    guardada.getMonto()
            );
        }

        return guardada;
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
