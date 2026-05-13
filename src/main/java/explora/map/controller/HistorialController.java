package explora.map.controller;

import explora.map.dto.EntradaHistorialResponseDTO;
import explora.map.entity.TipoElemento;
import explora.map.service.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/mapas/{mapaId}/historial")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialService historialService;

    @GetMapping
    public ResponseEntity<?> listar(
            @PathVariable Long mapaId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String usuaria,
            Principal principal) {

        String username = principal.getName();

        if (tipo != null) {
            TipoElemento tipoElemento;
            try {
                tipoElemento = TipoElemento.valueOf(tipo);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Tipo de elemento non válido");
            }
            return ResponseEntity.ok(historialService.listarPorMapaETipo(mapaId, tipoElemento, username));
        }

        if (usuaria != null) {
            return ResponseEntity.ok(historialService.listarPorMapaEUsuaria(mapaId, usuaria, username));
        }

        return ResponseEntity.ok(historialService.listarPorMapa(mapaId, username));
    }
}
