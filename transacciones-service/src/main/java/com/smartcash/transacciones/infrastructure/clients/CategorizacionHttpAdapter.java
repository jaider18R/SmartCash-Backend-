package com.smartcash.transacciones.infrastructure.clients;

import com.smartcash.transacciones.domain.ports.out.ClasificadorTransaccionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@Primary
public class CategorizacionHttpAdapter implements ClasificadorTransaccionPort {

    private static final Logger log = LoggerFactory.getLogger(CategorizacionHttpAdapter.class);

    private static final Map<String, UUID> CATEGORIAS_UUID = Map.of(
            "Mercado y Supermercado", UUID.fromString("00000000-0000-0000-0000-000000000002"),
            "Restaurantes y Comida", UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "Servicios y Suscripciones", UUID.fromString("00000000-0000-0000-0000-000000000003"),
            "Transporte y Viajes", UUID.fromString("00000000-0000-0000-0000-000000000004"),
            "Transferencias y Finanzas", UUID.fromString("00000000-0000-0000-0000-000000000005"),
            "Salud y Farmacia", UUID.fromString("00000000-0000-0000-0000-000000000006")
    );

    private final RestClient restClient;
    private final String url;

    public CategorizacionHttpAdapter(@Value("${smartcash.servicios.categorizacion.url:http://localhost:8000}") String url) {
        this.url = url;
        this.restClient = RestClient.builder().baseUrl(url).build();
    }

    @Override
    public ResultadoClasificacion clasificar(String comercio, BigDecimal monto) {
        try {
            Map<String, Object> body = Map.of(
                    "transaccion_id", Math.abs((int) (System.currentTimeMillis() % 1000000)),
                    "comercio", comercio,
                    "monto", monto.doubleValue()
            );

            Map<?, ?> response = restClient.post()
                    .uri("/clasificar")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("categoria")) {
                String categoriaStr = response.get("categoria").toString();
                double confianza = ((Number) response.get("nivel_confianza")).doubleValue();

                UUID idCategoria = CATEGORIAS_UUID.getOrDefault(
                        categoriaStr,
                        UUID.nameUUIDFromBytes(categoriaStr.getBytes())
                );
                return new ResultadoClasificacion(idCategoria, confianza);
            }
        } catch (Exception e) {
            log.warn("No fue posible consultar categorizacion-service ({}): {}. Retornando clasificacion por defecto.", url, e.getMessage());
        }

        return new ResultadoClasificacion(null, 0.0);
    }
}
