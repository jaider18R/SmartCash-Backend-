package com.smartcash.transacciones.domain.ports.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface VerificarPresupuestoUseCase {
    void verificarPresupuesto(UUID idUsuario, UUID idCategoria, LocalDate fechaGasto, BigDecimal montoNuevoGasto);
}
