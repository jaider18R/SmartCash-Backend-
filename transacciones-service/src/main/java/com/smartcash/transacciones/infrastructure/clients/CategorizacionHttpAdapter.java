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
            "Salud y Farmacia", UUID.fromString("00000000-0000-0000-0000-000000000006"),
            "Salario y Nómina", UUID.fromString("00000000-0000-0000-0000-000000000007"),
            "Inversiones y Rendimientos", UUID.fromString("00000000-0000-0000-0000-000000000008"),
            "Otros Ingresos", UUID.fromString("00000000-0000-0000-0000-000000000009")
    );

    private final RestClient restClient;
    private final String url;

    public CategorizacionHttpAdapter(@Value("${smartcash.servicios.categorizacion.url:http://localhost:8000}") String url) {
        this.url = url;
        this.restClient = RestClient.builder().baseUrl(url).build();
    }

    @Override
    public ResultadoClasificacion clasificar(String comercio, BigDecimal monto) {
        ResultadoClasificacionDetallada det = clasificarConDetalle(null, comercio, monto);
        return new ResultadoClasificacion(det.idCategoria(), det.confianza());
    }

    @Override
    public ResultadoClasificacionDetallada clasificarConDetalle(UUID idUsuario, String comercio, BigDecimal monto) {
        long txId = Math.abs((int) (System.currentTimeMillis() % 1000000));
        UUID idCategoria = null;
        double confianza = 0.0;
        boolean esInusual = false;
        String alerta = null;

        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("transaccion_id", txId);
            body.put("comercio", comercio);
            body.put("monto", monto.doubleValue());
            if (idUsuario != null) {
                body.put("usuario_id", idUsuario.toString());
            }

            Map<?, ?> response = restClient.post()
                    .uri("/clasificar")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("categoria")) {
                String categoriaStr = response.get("categoria").toString();
                confianza = ((Number) response.get("nivel_confianza")).doubleValue();

                idCategoria = CATEGORIAS_UUID.getOrDefault(
                        categoriaStr,
                        UUID.nameUUIDFromBytes(categoriaStr.getBytes())
                );
            }

            if (idUsuario != null) {
                Map<String, Object> patronBody = Map.of(
                        "usuario_id", idUsuario.toString(),
                        "transaccion_id", txId
                );

                Map<?, ?> patronResp = restClient.post()
                        .uri("/patrones/analizar")
                        .body(patronBody)
                        .retrieve()
                        .body(Map.class);

                if (patronResp != null && Boolean.TRUE.equals(patronResp.get("es_inusual"))) {
                    esInusual = true;
                    alerta = (String) patronResp.get("descripcion_alerta");
                }
            }

            return new ResultadoClasificacionDetallada(idCategoria, confianza, txId, esInusual, alerta);

        } catch (Exception e) {
            log.warn("No fue posible consultar categorizacion-service ({}): {}. Retornando clasificacion por defecto.", url, e.getMessage());
        }

        return new ResultadoClasificacionDetallada(null, 0.0, txId, false, null);
    }

    @Override
    public String obtenerExplicacion(long transaccionId) {
        try {
            Map<?, ?> response = restClient.get()
                    .uri("/clasificar/{id}/explicacion", transaccionId)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("explicacion")) {
                return response.get("explicacion").toString();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener explicacion para transaccion {}: {}", transaccionId, e.getMessage());
        }
        return "Clasificacion generada por modelo incremental de Machine Learning.";
    }
}

