package com.smartcash.transacciones.infrastructure.clients;

import com.smartcash.transacciones.domain.ports.out.ClasificadorTransaccionPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * ADAPTADOR TEMPORAL (mock). Simula la respuesta que en el futuro dara
 * categorizacion-service (Python/FastAPI) por HTTP.
 *
 * Por que existe esto: implementa ClasificadorTransaccionPort igual que lo
 * hara el adaptador real, asi que RegistrarTransaccionService ya puede
 * desarrollarse y probarse completo SIN esperar a que el otro microservicio
 * exista. Es el propio patron Puertos y Adaptadores resolviendo un problema
 * practico de calendario de desarrollo, no solo un ejercicio academico.
 *
 * Cuando categorizacion-service este listo:
 *   1. Se crea CategorizacionHttpAdapter implements ClasificadorTransaccionPort
 *   2. Se le pone @Primary (o se quita @Component de este mock)
 *   3. RegistrarTransaccionService no se toca -- sigue dependiendo de la interfaz.
 */
@Component
public class CategorizacionMockAdapter implements ClasificadorTransaccionPort {

    // Simulacion muy simple de un "clasificador por reglas" (nivel 1 de tu tesis)
    private static final Map<String, UUID> REGLAS_DEMO = Map.of(
            "rappi", UUID.fromString("00000000-0000-0000-0000-000000000001"),   // Domicilios
            "exito", UUID.fromString("00000000-0000-0000-0000-000000000002"),   // Mercado
            "d1",    UUID.fromString("00000000-0000-0000-0000-000000000002"),   // Mercado
            "netflix", UUID.fromString("00000000-0000-0000-0000-000000000003") // Suscripciones
    );

    @Override
    public ResultadoClasificacion clasificar(String comercio, BigDecimal monto) {
        String comercioNormalizado = comercio.toLowerCase();

        return REGLAS_DEMO.entrySet().stream()
                .filter(regla -> comercioNormalizado.contains(regla.getKey()))
                .findFirst()
                .map(regla -> new ResultadoClasificacion(regla.getValue(), 0.75))
                .orElseGet(() -> new ResultadoClasificacion(null, 0.0)); // "Otros" / sin categorizar
    }
}
