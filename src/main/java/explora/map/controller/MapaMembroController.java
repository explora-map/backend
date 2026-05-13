package explora.map.controller;

import explora.map.dto.MapaMembroResponseDTO;
import explora.map.dto.MapaMembroRolRequestDTO;
import explora.map.service.MapaMembroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MapaMembroController {

    private final MapaMembroService mapaMembroService;

    @GetMapping("/api/mapas/{mapaId}/membros")
    public ResponseEntity<List<MapaMembroResponseDTO>> listarMembros(
            @PathVariable Long mapaId, Authentication auth) {
        return ResponseEntity.ok(mapaMembroService.listarMembros(mapaId, auth.getName()));
    }

    @PatchMapping("/api/mapas/{mapaId}/membros/{username}/rol")
    public ResponseEntity<MapaMembroResponseDTO> cambiarRol(
            @PathVariable Long mapaId,
            @PathVariable String username,
            @Valid @RequestBody MapaMembroRolRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(
                mapaMembroService.cambiarRol(mapaId, username, dto.getRol(), auth.getName()));
    }

    @DeleteMapping("/api/mapas/{mapaId}/membros/{username}")
    public ResponseEntity<Void> eliminarMembro(
            @PathVariable Long mapaId,
            @PathVariable String username,
            Authentication auth) {
        mapaMembroService.eliminarMembro(mapaId, username, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
