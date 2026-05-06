package explora.map.controller;

import explora.map.dto.MarcadorRequestDTO;
import explora.map.dto.MarcadorResponseDTO;
import explora.map.service.MarcadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/marcadores")
@RequiredArgsConstructor
public class MarcadorEditController {

    private final MarcadorService marcadorService;

    @PutMapping("/{id}")
    public ResponseEntity<MarcadorResponseDTO> editar(@PathVariable Long id, @Valid @RequestBody MarcadorRequestDTO dto, Authentication auth) {
        return ResponseEntity.ok(marcadorService.editar(id, dto, auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        marcadorService.eliminar(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
