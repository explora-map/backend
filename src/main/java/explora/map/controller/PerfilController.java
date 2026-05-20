package explora.map.controller;

import explora.map.dto.PerfilRequestDTO;
import explora.map.dto.PerfilResponseDTO;
import explora.map.service.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping
    public ResponseEntity<PerfilResponseDTO> obterPerfil(Authentication authentication) {
        return ResponseEntity.ok(perfilService.obterPerfil(authentication.getName()));
    }

    @PatchMapping
    public ResponseEntity<PerfilResponseDTO> actualizarPerfil(
            @Valid @RequestBody PerfilRequestDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(perfilService.actualizarPerfil(authentication.getName(), dto));
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminarConta(Authentication authentication) {
        perfilService.eliminar(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
