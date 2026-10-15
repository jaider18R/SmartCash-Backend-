package com.smartcash.transacciones.application.services;

import com.smartcash.transacciones.domain.model.Presupuesto;
import com.smartcash.transacciones.domain.ports.in.GestionarPresupuestoUseCase;
import com.smartcash.transacciones.domain.ports.in.VerificarPresupuestoUseCase;
import com.smartcash.transacciones.domain.ports.out.AlertaNotificacionPort;
import com.smartcash.transacciones.domain.ports.out.RepositorioPresupuestoPort;
import com.smartcash.transacciones.domain.ports.out.RepositorioTransaccionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PresupuestoService implements GestionarPresupuestoUseCase, VerificarPresupuestoUseCase {

    private static final Logger log = LoggerFactory.getLogger(PresupuestoService.class);

    private final RepositorioPresupuestoPort repositorioPresupuesto;
    private final RepositorioTransaccionPort repositorioTransaccion;
    private final AlertaNotificacionPort alertaNotificacionPort;

    public PresupuestoService(RepositorioPresupuestoPort repositorioPresupuesto,
                              RepositorioTransaccionPort repositorioTransaccion,
                              AlertaNotificacionPort alertaNotificacionPort) {
        this.repositorioPresupuesto = repositorioPresupuesto;
        this.repositorioTransaccion = repositorioTransaccion;
        this.alertaNotificacionPort = alertaNotificacionPort;
    }

    @Override
    public Presupuesto crearOActualizar(UUID idUsuario, UUID idCategoria, String nombreCategoria,
                                         BigDecimal montoLimite, int mes, int anio) {
        Optional<Presupuesto> existente = repositorioPresupuesto.buscarPorUsuarioYCategoriaYPeriodo(
                idUsuario, idCategoria, mes, anio);

        Presupuesto presupuestoAGuardar;
        if (existente.isPresent()) {
            presupuestoAGuardar = new Presupuesto(
                    existente.get().getId(),
                    idUsuario,
                    idCategoria,
                    nombreCategoria,
                    montoLimite,
                    mes,
                    anio
            );
        } else {
            presupuestoAGuardar = Presupuesto.nuevo(
                    idUsuario,
                    idCategoria,
                    nombreCategoria,
                    montoLimite,
                    mes,
                    anio
            );
        }

        Presupuesto guardado = repositorioPresupuesto.guardar(presupuestoAGuardar);

        verificarPresupuesto(idUsuario, idCategoria, LocalDate.of(anio, mes, 1), BigDecimal.ZERO);

        return guardado;
    }

    @Override
    public List<Presupuesto> listarPorUsuario(UUID idUsuario) {
        return repositorioPresupuesto.listarPorUsuario(idUsuario);
    }

    @Override
    public void verificarPresupuesto(UUID idUsuario, UUID idCategoria, LocalDate fechaGasto, BigDecimal montoNuevoGasto) {
        if (idUsuario == null || idCategoria == null || fechaGasto == null) {
            return;
        }

        int mes = fechaGasto.getMonthValue();
        int anio = fechaGasto.getYear();

        Optional<Presupuesto> presupuestoOpt = repositorioPresupuesto.buscarPorUsuarioYCategoriaYPeriodo(
                idUsuario, idCategoria, mes, anio);

        if (presupuestoOpt.isEmpty()) {
            return;
        }

        Presupuesto presupuesto = presupuestoOpt.get();
        if (presupuesto.getMontoLimite() == null || presupuesto.getMontoLimite().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        YearMonth ym = YearMonth.of(anio, mes);
        LocalDate primerDia = ym.atDay(1);
        LocalDate ultimoDia = ym.atEndOfMonth();

        BigDecimal gastoAcumulado = repositorioTransaccion.calcularGastoAcumulado(
                idUsuario, idCategoria, primerDia, ultimoDia);

        if (gastoAcumulado == null) {
            gastoAcumulado = BigDecimal.ZERO;
        }

        BigDecimal porcentaje = gastoAcumulado
                .divide(presupuesto.getMontoLimite(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Verificacion Presupuesto: Categoria='{}', GastoAcumulado={}, Limite={}, Porcentaje={}%",
                presupuesto.getNombreCategoria(), gastoAcumulado, presupuesto.getMontoLimite(), porcentaje);

        if (porcentaje.compareTo(new BigDecimal("80")) >= 0) {
            String correoUsuario = obtenerCorreoUsuario();
            String nombreUsuario = obtenerNombreUsuario();
            log.info("Disparando envio de alerta de presupuesto a: correo='{}', nombre='{}'", correoUsuario, nombreUsuario);
            alertaNotificacionPort.enviarAlertaPresupuesto(
                    correoUsuario,
                    nombreUsuario,
                    presupuesto.getNombreCategoria(),
                    porcentaje
            );
        } else {
            log.info("Porcentaje {}% no supera el umbral del 80%. No se emite alerta.", porcentaje);
        }
    }

    private String obtenerCorreoUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof java.util.Map<?, ?> map) {
            Object nombre = map.get("nombre");
            if (nombre instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }
}
