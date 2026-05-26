package explora.map.controller;

import explora.map.dto.MapaResponseDTO;
import explora.map.service.MapaGardadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Controlador REST para gardar e desgardar mapas de acceso rápido. */
@Tag(name = "Mapas gardados", description = "Gardar e desgardar mapas para acceso rápido")
@RestController
@RequiredArgsConstructor
public class MapaGardadoController {

    private final MapaGardadoService mapaGardadoService;

    @Operation(summary = "Listar os mapas gardados pola usuaria autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de mapas gardados"),
        @ApiResponse(responseCode = "401", description = "Non autenticada")
    })
    @GetMapping("/api/mapas/gardados")
    public ResponseEntity<List<MapaResponseDTO>> obterMapasGardados(Authentication auth) {
        return ResponseEntity.ok(mapaGardadoService.obterMapasGardados(auth.getName()));
    }

    @Operation(summary = "Gardar un mapa nos favoritos da usuaria autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mapa gardado correctamente"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "404", description = "Mapa non atopado"),
        @ApiResponse(responseCode = "409", description = "O mapa xa estaba gardado")
    })
    @PostMapping("/api/mapas/{mapaId}/gardar")
    public ResponseEntity<Void> gardarMapa(@PathVariable Long mapaId, Authentication auth) {
        mapaGardadoService.gardarMapa(mapaId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar un mapa dos favoritos da usuaria autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mapa desgardado correctamente"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "404", description = "Mapa non atopado ou non estaba gardado")
    })
    @DeleteMapping("/api/mapas/{mapaId}/gardar")
    public ResponseEntity<Void> desgardarMapa(@PathVariable Long mapaId, Authentication auth) {
        mapaGardadoService.desgardarMapa(mapaId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
