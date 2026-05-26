package explora.map.controller;

import explora.map.dto.MapaMembroResponseDTO;
import explora.map.dto.MapaMembroRolRequestDTO;
import explora.map.service.MapaMembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Controlador REST para a xestión de membros e roles dentro dun mapa colaborativo. */
@Tag(name = "Membros", description = "Xestión de membros e roles dentro dun mapa colaborativo")
@RestController
@RequiredArgsConstructor
public class MapaMembroController {

    private final MapaMembroService mapaMembroService;

    @Operation(summary = "Listar os membros dun mapa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de membros co seu rol"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para ver os membros deste mapa"),
        @ApiResponse(responseCode = "404", description = "Mapa non atopado")
    })
    @GetMapping("/api/mapas/{mapaId}/membros")
    public ResponseEntity<List<MapaMembroResponseDTO>> listarMembros(
            @PathVariable Long mapaId, Authentication auth) {
        return ResponseEntity.ok(mapaMembroService.listarMembros(mapaId, auth.getName()));
    }

    @Operation(summary = "Cambiar o rol dun membro dentro dun mapa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Rol inválido"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para cambiar roles neste mapa"),
        @ApiResponse(responseCode = "404", description = "Mapa ou membro non atopado")
    })
    @PatchMapping("/api/mapas/{mapaId}/membros/{username}/rol")
    public ResponseEntity<MapaMembroResponseDTO> cambiarRol(
            @PathVariable Long mapaId,
            @PathVariable String username,
            @Valid @RequestBody MapaMembroRolRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(
                mapaMembroService.cambiarRol(mapaId, username, dto.getRol(), auth.getName()));
    }

    @Operation(summary = "Eliminar un membro dun mapa")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Membro eliminado correctamente"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para eliminar membros deste mapa"),
        @ApiResponse(responseCode = "404", description = "Mapa ou membro non atopado")
    })
    @DeleteMapping("/api/mapas/{mapaId}/membros/{username}")
    public ResponseEntity<Void> eliminarMembro(
            @PathVariable Long mapaId,
            @PathVariable String username,
            Authentication auth) {
        mapaMembroService.eliminarMembro(mapaId, username, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
