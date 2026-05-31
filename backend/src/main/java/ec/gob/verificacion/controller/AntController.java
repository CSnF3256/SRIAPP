package ec.gob.verificacion.controller;

import ec.gob.verificacion.service.AntService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ant")
@RequiredArgsConstructor
public class AntController {

    private final AntService antService;

    /**
     * GET /api/ant/licencia?cedula=XXXXXXXX&placa=ABC1234
     *
     * Respuesta:
     * {
     *   "datos": { "puntosActuales": 28, "citaciones": [...], ... },
     *   "fromCache": true | false
     * }
     */
    @GetMapping("/licencia")
    public ResponseEntity<Map<String, Object>> consultarLicencia(
            @RequestParam String cedula,
            @RequestParam String placa) {

        if (cedula.length() < 8 || cedula.length() > 10) {
            return ResponseEntity.badRequest()
                .body(Map.of("mensaje", "Cédula inválida"));
        }
        if (placa.length() < 6) {
            return ResponseEntity.badRequest()
                .body(Map.of("mensaje", "Placa inválida"));
        }

        try {
            Map<String, Object> result = antService.consultarConCache(
                cedula.trim(),
                placa.toUpperCase().replaceAll("[^A-Z0-9]","")
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                .body(Map.of(
                    "mensaje", e.getMessage(),
                    "fromCache", false,
                    "disponible", false
                ));
        }
    }

    /**
     * DELETE /api/ant/licencia/cache?cedula=X&placa=Y
     * Endpoint admin para invalidar caché.
     */
    @DeleteMapping("/licencia/cache")
    public ResponseEntity<Map<String, Object>> invalidarCache(
            @RequestParam String cedula,
            @RequestParam String placa) {

        boolean deleted = antService.invalidarCache(cedula, placa);
        return ResponseEntity.ok(Map.of(
            "invalidado", deleted,
            "clave", cedula + ":" + placa
        ));
    }
}
