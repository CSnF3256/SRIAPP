package ec.gob.verificacion.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Consulta el portal de citaciones de la ANT.
 * La ANT tiene BAJA DISPONIBILIDAD → decoramos con:
 *   - @CircuitBreaker (abre si >50% de fallas)
 *   - @Retry (máx 2 reintentos)
 *   - @TimeLimiter (timeout 20s)
 * El caché se maneja en AntService para poder controlar fromCache.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AntClient {

    private static final String ANT_URL =
        "https://consultaweb.ant.gob.ec/PortalWEB/paginas/clientes/" +
        "clp_grid_citaciones.jsp";

    private final WebClient webClient;

    @CircuitBreaker(name = "ant-service", fallbackMethod = "fallback")
    @Retry(name = "ant-service")
    @TimeLimiter(name = "ant-service")
    public CompletableFuture<Map<String, Object>> consultarLicencia(
            String cedula, String placa) {

        String url = ANT_URL +
            "?ps_tipo_identificacion=CED" +
            "&ps_identificacion=" + cedula +
            "&ps_placa=" + placa;

        log.info("Consultando ANT: cedula={}, placa={}", cedula, placa);

        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .map(this::parseAntHtml)
            .toFuture();
    }

    /**
     * Parsea la tabla HTML que devuelve la ANT con Jsoup.
     * La página devuelve una tabla con columnas:
     *   Fecha, Tipo, Descripción, Puntos, Estado, etc.
     */
    private Map<String, Object> parseAntHtml(String html) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> citaciones = new ArrayList<>();
        int puntosTotales = 30;
        int puntosDescontados = 0;

        try {
            Document doc = Jsoup.parse(html);

            // Buscar la tabla de citaciones
            Elements filas = doc.select("table tr");
            boolean esPrimeraFila = true;

            for (Element fila : filas) {
                if (esPrimeraFila) { esPrimeraFila = false; continue; } // skip header

                Elements cols = fila.select("td");
                if (cols.size() >= 4) {
                    Map<String, String> cit = new HashMap<>();
                    cit.put("fecha",       cols.get(0).text().trim());
                    cit.put("descripcion", cols.size() > 2 ? cols.get(2).text().trim() : "—");
                    String puntosTxt = cols.size() > 3 ? cols.get(3).text().trim().replaceAll("[^0-9]","") : "0";
                    int pts = puntosTxt.isEmpty() ? 0 : Integer.parseInt(puntosTxt);
                    cit.put("puntos", String.valueOf(pts));
                    cit.put("estado", cols.size() > 4 ? cols.get(4).text().trim() : "—");
                    citaciones.add(cit);
                    puntosDescontados += pts;
                }
            }

            // Si la página tiene el resumen de puntos directamente
            Elements puntosEl = doc.select("[id*=puntos], [class*=puntos]");
            if (!puntosEl.isEmpty()) {
                try {
                    String pTxt = puntosEl.first().text().replaceAll("[^0-9]","");
                    if (!pTxt.isEmpty()) puntosTotales = Integer.parseInt(pTxt);
                } catch (NumberFormatException ignored) {}
            }

        } catch (Exception e) {
            log.error("Error parseando HTML de ANT: {}", e.getMessage());
            result.put("error", "No se pudo interpretar la respuesta de la ANT");
        }

        result.put("puntosActuales", puntosTotales - puntosDescontados);
        result.put("puntosDescontados", puntosDescontados);
        result.put("citaciones", citaciones);
        result.put("totalCitaciones", citaciones.size());
        return result;
    }

    /**
     * Fallback cuando el circuit breaker está abierto
     * o se agotan los reintentos.
     */
    public CompletableFuture<Map<String, Object>> fallback(
            String cedula, String placa, Throwable ex) {
        log.warn("Circuit breaker ANT activo para cedula={}, placa={}: {}",
            cedula, placa, ex.getMessage());
        return CompletableFuture.failedFuture(
            new RuntimeException("La ANT no está disponible en este momento. " +
                "Intenta más tarde o usa datos en caché."));
    }
}
