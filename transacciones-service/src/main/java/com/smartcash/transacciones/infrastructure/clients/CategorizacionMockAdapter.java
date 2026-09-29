package com.smartcash.transacciones.infrastructure.clients;

import com.smartcash.transacciones.domain.ports.out.ClasificadorTransaccionPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class CategorizacionMockAdapter implements ClasificadorTransaccionPort {

    private static final Map<String, UUID> REGLAS_DEMO = Map.of(
            "rappi", UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "exito", UUID.fromString("00000000-0000-0000-0000-000000000002"),
            "d1",    UUID.fromString("00000000-0000-0000-0000-000000000002"),
            "netflix", UUID.fromString("00000000-0000-0000-0000-000000000003")
    );

    @Override
    public ResultadoClasificacion clasificar(String comercio, BigDecimal monto) {
        String comercioNormalizado = comercio.toLowerCase();

        return REGLAS_DEMO.entrySet().stream()
                .filter(regla -> comercioNormalizado.contains(regla.getKey()))
                .findFirst()
                .map(regla -> new ResultadoClasificacion(regla.getValue(), 0.75))
                .orElseGet(() -> new ResultadoClasificacion(null, 0.0));
    }
}
