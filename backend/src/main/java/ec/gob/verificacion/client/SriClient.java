package ec.gob.verificacion.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SriClient {

    private static final String SRI_BASE = "https://srienlinea.sri.gob.ec";

    private final WebClient webClient;

    public Mono<Boolean> existeContribuyente(String ruc) {

        String url = SRI_BASE
                + "/sri-catastro-sujeto-servicio-internet/rest/"
                + "ConsolidadoContribuyente/existePorNumeroRuc?numeroRuc="
                + ruc;

        log.debug("SRI verificar contribuyente: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Error HTTP al verificar contribuyente en SRI: {}", ex.getMessage());
                    return Mono.just(false);
                })
                .onErrorResume(ex -> {
                    log.error("Error SRI existeContribuyente: {}", ex.getMessage());
                    return Mono.error(new RuntimeException("SRI no disponible: " + ex.getMessage()));
                });
    }

    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> obtenerPorRuc(String ruc) {

        String url = SRI_BASE
                + "/sri-catastro-sujeto-servicio-internet/rest/"
                + "ConsolidadoContribuyente/obtenerPorNumerosRuc?&ruc="
                + ruc;

        log.debug("SRI obtener persona: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Map.class)
                .next()
                .map(m -> (Map<String, Object>) m)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No se encontró información para el RUC: " + ruc)))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Error HTTP al obtener persona en SRI: {}", ex.getMessage());
                    return Mono.error(new RuntimeException("Error al consultar persona en SRI: " + ex.getMessage()));
                })
                .onErrorResume(ex -> {
                    log.error("Error SRI obtenerPorRuc: {}", ex.getMessage());
                    return Mono.error(ex);
                });
    }

    
    
@SuppressWarnings("unchecked")
public Mono<Map<String, Object>> obtenerVehiculoPorPlaca(String placa) {

    String url = SRI_BASE
            + "/sri-matriculacion-vehicular-recaudacion-servicio-internet/rest/"
            + "BaseVehiculo/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn"
            + "?numeroPlacaCampvCpn="
            + placa;

    log.info("Consultando vehículo en SRI: {}", url);

    return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(Map.class)
            .map(respuesta -> (Map<String, Object>) respuesta)
            .flatMap(respuesta -> {

                /*
                 * CASO 1:
                 * El SRI responde vehículo encontrado directamente:
                 * {
                 *   "codigoVehiculo": 2009293,
                 *   "numeroPlaca": "PBD7436",
                 *   "descripcionMarca": "RENAULT",
                 *   ...
                 * }
                 */
                if (respuesta.containsKey("codigoVehiculo") || respuesta.containsKey("numeroPlaca")) {
                    return Mono.just(respuesta);
                }

                /*
                 * CASO 2:
                 * El SRI responde vehículo no encontrado:
                 * {
                 *   "objeto": null,
                 *   "mensajeServidor": { "texto": "El vehículo no existe" },
                 *   "data": []
                 * }
                 */
                Object objeto = respuesta.get("objeto");
                Object mensajeServidor = respuesta.get("mensajeServidor");

                if (objeto == null) {
                    String mensaje = "Vehículo no encontrado con placa: " + placa;

                    if (mensajeServidor instanceof Map<?, ?> mensajeMap) {
                        Object texto = mensajeMap.get("texto");
                        if (texto != null) {
                            mensaje = texto.toString();
                        }
                    }

                    return Mono.error(new IllegalArgumentException(mensaje));
                }

                /*
                 * CASO 3:
                 * Si el SRI algún día responde con objeto.
                 */
                if (objeto instanceof Map<?, ?> vehiculoMap) {
                    return Mono.just((Map<String, Object>) vehiculoMap);
                }

                return Mono.error(new RuntimeException("Formato inesperado en respuesta del SRI"));
            })
            .onErrorResume(WebClientResponseException.class, ex -> {
                log.error("Error HTTP al consultar vehículo. Status: {}, Body: {}",
                        ex.getStatusCode(),
                        ex.getResponseBodyAsString());

                return Mono.error(new RuntimeException(
                        "Error obteniendo datos del vehículo: " + ex.getStatusCode()
                ));
            });
}
}