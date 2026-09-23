package com.smartcash.transacciones.domain.ports.out;

import java.math.BigDecimal;

/**
 * Puerto de salida hacia el servicio de categorizacion (Python/FastAPI).
 *
 * OCP + DIP en su maxima expresion aqui: HOY la implementacion es un
 * adaptador "mock" (CategorizacionMockAdapter). El dia que categorizacion-service
 * exista de verdad, se crea un CategorizacionHttpAdapter que llame por HTTP
 * y se implemente esta MISMA interfaz. RegistrarTransaccionService (la clase
 * que usa este puerto) no se modifica en absoluto.
 */
public interface ClasificadorTransaccionPort {
    ResultadoClasificacion clasificar(String comercio, BigDecimal monto);

    record ResultadoClasificacion(java.util.UUID idCategoria, double confianza) {}
}
