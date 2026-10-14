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
    private final com.smartcash.transacciones.domain.ports.out.AlertaNotificacionPort alertaNotificacionPort;

    public RegistrarTransaccionService(RepositorioTransaccionPort repositorioTransaccion,
                                        ClasificadorTransaccionPort clasificador,
                                        com.smartcash.transacciones.domain.ports.in.VerificarPresupuestoUseCase verificarPresupuestoUseCase,
                                        com.smartcash.transacciones.domain.ports.out.AlertaNotificacionPort alertaNotificacionPort) {
        this.repositorioTransaccion = repositorioTransaccion;
        this.clasificador = clasificador;
        this.verificarPresupuestoUseCase = verificarPresupuestoUseCase;
        this.alertaNotificacionPort = alertaNotificacionPort;
    }

    @Override
    public Transaccion registrar(UUID idUsuario, BigDecimal monto, LocalDate fecha,
                                  String comercio, String tipoMovimiento) {
        validar(monto, comercio, tipoMovimiento);

        Transaccion transaccion = Transaccion.nueva(idUsuario, monto, fecha, comercio, tipoMovimiento);

        ClasificadorTransaccionPort.ResultadoClasificacionDetallada resultado =
                clasificador.clasificarConDetalle(idUsuario, comercio, monto);

        Transaccion transaccionCategorizada = transaccion.conCategoria(
                resultado.idCategoria(), resultado.confianza());

        Transaccion guardada = repositorioTransaccion.guardar(transaccionCategorizada);

        if ("gasto".equalsIgnoreCase(tipoMovimiento)) {
            if (resultado.esInusual()) {
                String correo = obtenerCorreoUsuario();
                String nombre = obtenerNombreUsuario();
                alertaNotificacionPort.enviarAlertaGastoInusual(
                        correo, nombre, comercio, monto, resultado.alertaInusual()
                );
            }

            if (guardada.getIdCategoria() != null) {
                verificarPresupuestoUseCase.verificarPresupuesto(
                        guardada.getIdUsuario(),
                        guardada.getIdCategoria(),
                        guardada.getFecha(),
                        guardada.getMonto()
                );
            }
        }

        return guardada;
    }

    @Override
    public Transaccion actualizar(UUID idUsuario, UUID idTransaccion, BigDecimal monto, LocalDate fecha,
                                   String comercio, String tipoMovimiento, UUID idCategoria) {
        validar(monto, comercio, tipoMovimiento);

        Transaccion existente = repositorioTransaccion.buscarPorId(idTransaccion)
                .orElseThrow(() -> new TransaccionInvalidaException("Transaccion no encontrada"));

        if (!existente.getIdUsuario().equals(idUsuario)) {
            throw new TransaccionInvalidaException("No tiene permisos para modificar esta transaccion");
        }

        UUID categoriaFinal = idCategoria != null ? idCategoria : existente.getIdCategoria();

        Transaccion actualizada = new Transaccion(
                existente.getId(),
                idUsuario,
                categoriaFinal,
                monto,
                fecha,
                comercio,
                tipoMovimiento,
                existente.getConfianzaCategorizacion()
        );

        Transaccion guardada = repositorioTransaccion.guardar(actualizada);

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

    @Override
    public void eliminar(UUID idUsuario, UUID idTransaccion) {
        Transaccion existente = repositorioTransaccion.buscarPorId(idTransaccion)
                .orElseThrow(() -> new TransaccionInvalidaException("Transaccion no encontrada"));

        if (!existente.getIdUsuario().equals(idUsuario)) {
            throw new TransaccionInvalidaException("No tiene permisos para eliminar esta transaccion");
        }

        repositorioTransaccion.eliminar(idTransaccion);
    }

    @Override
    public java.util.List<Transaccion> listarPorUsuario(UUID idUsuario) {
        return repositorioTransaccion.listarPorUsuario(idUsuario);
    }


    private String obtenerCorreoUsuario() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.getDetails() instanceof java.util.Map<?, ?> map) {
                Object email = map.get("email");
                if (email instanceof String s && !s.isBlank()) {
                    return s;
                }
            } else if (auth.getDetails() instanceof String email && !email.isBlank()) {
                return email;
            }
        }
        return null;
    }

    private String obtenerNombreUsuario() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof java.util.Map<?, ?> map) {
            Object nombre = map.get("nombre");
            if (nombre instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return null;
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
