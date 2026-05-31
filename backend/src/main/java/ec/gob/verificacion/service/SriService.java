package ec.gob.verificacion.service;

import ec.gob.verificacion.client.SriClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SriService {

    private final SriClient sriClient;

    public Mono<Boolean> verificarContribuyente(String ruc) {
        return sriClient.existeContribuyente(ruc);
    }

    /**
     * Valida que sea persona natural.
     */
    public Mono<Map<String, Object>> obtenerPersonaNatural(String ruc) {
    return sriClient.obtenerPorRuc(ruc)
            .flatMap(datos -> {
                Object tipo = datos.get("tipoContribuyente");

                if (tipo == null || !tipo.toString().toUpperCase().contains("PERSONA NATURAL")) {
                    return Mono.error(new IllegalArgumentException(
                            "El RUC no corresponde a una persona natural"
                    ));
                }

                return Mono.just(datos);
            })
            .switchIfEmpty(Mono.error(
                    new IllegalArgumentException("No se encontraron datos para el RUC: " + ruc)
            ));
}

    public Mono<Map<String, Object>> obtenerVehiculo(String placa) {
        return sriClient.obtenerVehiculoPorPlaca(placa)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("Vehículo no encontrado con placa: " + placa)
                ));
    }
}