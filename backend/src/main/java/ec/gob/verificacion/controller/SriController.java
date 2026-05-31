package ec.gob.verificacion.controller;

import ec.gob.verificacion.service.SriService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/sri")
@RequiredArgsConstructor
public class SriController {

    private final SriService sriService;
    private final WebClient webClient;

    @GetMapping("/contribuyente/verificar")
    public Mono<ResponseEntity<Map<String, Object>>> verificarContribuyente(@RequestParam String ruc) {

        if (ruc == null || !ruc.matches("\\d{13}")) {
            return Mono.just(
                    ResponseEntity.badRequest().body(
                            Map.<String, Object>of(
                                    "error", "RUC debe tener 13 dígitos",
                                    "esContribuyente", false
                            )
                    )
            );
        }

        return sriService.verificarContribuyente(ruc)
                .map(existe -> ResponseEntity.ok(
                        Map.<String, Object>of(
                                "ruc", ruc,
                                "esContribuyente", existe
                        )
                ))
                .onErrorResume(ex -> Mono.just(
                        ResponseEntity.internalServerError().body(
                                Map.<String, Object>of(
                                        "error", "Error interno: " + ex.getMessage(),
                                        "esContribuyente", false
                                )
                        )
                ));
    }

    @GetMapping("/persona")
    public Mono<ResponseEntity<Map<String, Object>>> obtenerPersona(@RequestParam String ruc) {

        if (ruc == null || !ruc.matches("\\d{10}|\\d{13}")) {
            return Mono.just(
                    ResponseEntity.badRequest().body(
                            Map.<String, Object>of(
                                    "error", "Debe ingresar una cédula de 10 dígitos o RUC de 13 dígitos"
                            )
                    )
            );
        }

        return sriService.obtenerPersonaNatural(ruc)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, ex ->
                        Mono.just(
                                ResponseEntity.badRequest().body(
                                        Map.<String, Object>of(
                                                "error", ex.getMessage()
                                        )
                                )
                        )
                )
                .onErrorResume(ex ->
                        Mono.just(
                                ResponseEntity.internalServerError().body(
                                        Map.<String, Object>of(
                                                "error", "Error interno: " + ex.getMessage()
                                        )
                                )
                        )
                );
    }

    @GetMapping("/vehiculo")
    public Mono<ResponseEntity<Map<String, Object>>> obtenerVehiculo(@RequestParam String placa) {

        if (placa == null || placa.isBlank()) {
            return Mono.just(
                    ResponseEntity.badRequest().body(
                            Map.<String, Object>of(
                                    "error", "La placa es obligatoria"
                            )
                    )
            );
        }

        String placaNorm = placa.toUpperCase().replaceAll("[^A-Z0-9]", "");

        return sriService.obtenerVehiculo(placaNorm)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, ex ->
                        Mono.just(
                                ResponseEntity.status(404).body(
                                        Map.<String, Object>of(
                                                "error", ex.getMessage()
                                        )
                                )
                        )
                )
                .onErrorResume(ex ->
                        Mono.just(
                                ResponseEntity.internalServerError().body(
                                        Map.<String, Object>of(
                                                "error", "Error interno: " + ex.getMessage()
                                        )
                                )
                        )
                );
    }

    @GetMapping("/persona/raw")
    public Mono<ResponseEntity<String>> obtenerPersonaRaw(@RequestParam String ruc) {

        if (ruc == null || !ruc.matches("\\d{10}|\\d{13}")) {
            return Mono.just(
                    ResponseEntity.badRequest()
                            .body("Debe ingresar una cédula de 10 dígitos o RUC de 13 dígitos")
            );
        }

        String url = "https://srienlinea.sri.gob.ec"
                + "/sri-catastro-sujeto-servicio-internet/rest/"
                + "ConsolidadoContribuyente/obtenerPorNumerosRuc?&ruc=" + ruc;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok)
                .onErrorResume(ex ->
                        Mono.just(
                                ResponseEntity.internalServerError()
                                        .body("Error al consultar SRI: " + ex.getMessage())
                        )
                );
    }

    @GetMapping("/vehiculo/raw")
    public Mono<ResponseEntity<String>> obtenerVehiculoRaw(@RequestParam String placa) {

        if (placa == null || placa.isBlank()) {
            return Mono.just(
                    ResponseEntity.badRequest()
                            .body("La placa es obligatoria")
            );
        }

        String placaNorm = placa.toUpperCase().replaceAll("[^A-Z0-9]", "");

        String url = "https://srienlinea.sri.gob.ec"
                + "/sri-matriculacion-vehicular-recaudacion-servicio-internet/rest/"
                + "BaseVehiculo/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn"
                + "?numeroPlacaCampvCpn="
                + placaNorm;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok)
                .onErrorResume(ex ->
                        Mono.just(
                                ResponseEntity.internalServerError()
                                        .body("Error al consultar vehículo: " + ex.getMessage())
                        )
                );
    }
}