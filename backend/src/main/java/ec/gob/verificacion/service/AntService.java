package ec.gob.verificacion.service;

import ec.gob.verificacion.client.AntClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementa el patrón CACHE ASIDE para la ANT:
 *
 *  1. Buscar en Redis por clave (cedula:placa)
 *  2. Si HIT → devolver desde caché (fromCache=true)
 *  3. Si MISS → llamar a AntClient
 *     a. Si OK  → guardar en Redis con TTL 24h, devolver (fromCache=false)
 *     b. Si KO  → lanzar excepción (el frontend muestra error + opción reintentar)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AntService {

    private static final String CACHE_PREFIX = "ant:licencia:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final AntClient antClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    public Map<String, Object> consultarConCache(String cedula, String placa)
            throws Exception {

        String cacheKey = CACHE_PREFIX + cedula + ":" + placa;

        // ── PASO 1: Cache lookup ─────────────────────────────
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("Cache HIT para key={}", cacheKey);
            Map<String, Object> datos = (Map<String, Object>) cached;
            Map<String, Object> response = new HashMap<>();
            response.put("datos", datos);
            response.put("fromCache", true);
            return response;
        }

        log.info("Cache MISS para key={} — consultando ANT", cacheKey);

        // ── PASO 2: Llamar a la ANT (con circuit breaker) ────
        Map<String, Object> datos = antClient
            .consultarLicencia(cedula, placa)
            .get(); // bloqueo justificado (endpoint síncrono)

        // Agregar timestamp para el frontend
        datos.put("cacheAge", "ahora mismo");
        datos.put("consultadoEn", Instant.now().toString());

        // ── PASO 3: Guardar en caché SOLO si la respuesta es útil
        if (!datos.containsKey("error")) {
            log.info("Guardando en caché key={} TTL={}", cacheKey, CACHE_TTL);
            redisTemplate.opsForValue().set(cacheKey, datos, CACHE_TTL);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("datos", datos);
        response.put("fromCache", false);
        return response;
    }

    /** Invalidar caché manualmente (útil para testing o admin) */
    public boolean invalidarCache(String cedula, String placa) {
        String cacheKey = CACHE_PREFIX + cedula + ":" + placa;
        Boolean deleted = redisTemplate.delete(cacheKey);
        log.info("Cache invalidado para key={}: {}", cacheKey, deleted);
        return Boolean.TRUE.equals(deleted);
    }
}
