package explora.map.controller;

import explora.map.dto.HistorialResponseDTO;
import explora.map.entity.TipoElemento;
import explora.map.service.HistorialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/** Controlador REST para o historial de cambios dun mapa. Endpoints baixo /api/mapas/{mapaId}/historial. */
@Tag(name = "Historial", description = "Consulta do historial de cambios dun mapa colaborativo")
@RestController
@RequestMapping("/api/mapas/{mapaId}/historial")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialService historialService;

    @Operation(summary = "Listar o historial de actividade dun mapa, con filtro opcional por tipo ou usuaria")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de entradas do historial, ordenada da máis recente á máis antiga"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para ver o historial deste mapa"),
        @ApiResponse(responseCode = "404", description = "Mapa non atopado")
    })
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
