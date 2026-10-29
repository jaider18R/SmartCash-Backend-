package com.smartcash.transacciones.infrastructure.clients;

import com.smartcash.transacciones.domain.ports.out.RegistroLogPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Adaptador que envia los logs al microservicio logs-service por HTTP.
 * El envio es en segundo plano y los fallos se ignoran: registrar un log nunca debe romper una peticion.
 */
@Component
public class LogsRestClientAdapter implements RegistroLogPort {

    private static final Logger log = LoggerFactory.getLogger(LogsRestClientAdapter.class);
    private static final String SERVICIO = "transacciones-service";

    private final RestClient restClient;
    private final String url;
    private final boolean habilitado;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "logs-remoto");
        t.setDaemon(true);
        return t;
    });

    public LogsRestClientAdapter(
            @Value("${smartcash.servicios.logs.url:http://localhost:8085}") String url,
            @Value("${smartcash.logs.habilitado:true}") boolean habilitado) {
        this.url = url + "/api/logs";
        this.habilitado = habilitado;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000);
        factory.setReadTimeout(2000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public void registrar(String nivel, String mensaje, String detalle,
                          String ruta, String metodo, Integer estadoHttp, String usuario) {
        if (!habilitado) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("servicio", SERVICIO);
        payload.put("nivel", nivel);
        payload.put("mensaje", mensaje);
        payload.put("detalle", detalle);
        payload.put("ruta", ruta);
        payload.put("metodo", metodo);
        payload.put("estado_http", estadoHttp);
        payload.put("usuario", usuario);
        try {
            executor.submit(() -> enviar(payload));
        } catch (RejectedExecutionException e) {
            log.debug("No se pudo encolar el log remoto: {}", e.getMessage());
        }
    }

    private void enviar(Map<String, Object> payload) {
        try {
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.debug("logs-service no disponible, el log solo quedo en consola: {}", e.getMessage());
        }
    }
}
