package explora.map.controller;

import explora.map.dto.MarcadorRequestDTO;
import explora.map.dto.MarcadorResponseDTO;
import explora.map.service.MarcadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
public class MarcadorController {

    private final MarcadorService marcadorService;

    @GetMapping("/{mapaId}/marcadores")
    public ResponseEntity<List<MarcadorResponseDTO>> listarPorMapa(@PathVariable Long mapaId, Authentication auth) {
        return ResponseEntity.ok(marcadorService.listarPorMapa(mapaId, auth.getName()));
    }

    @PostMapping("/{mapaId}/marcadores")
    public ResponseEntity<MarcadorResponseDTO> crear(@PathVariable Long mapaId, @Valid @RequestBody MarcadorRequestDTO dto, Authentication auth) {
        return ResponseEntity.status(201).body(marcadorService.crear(mapaId, dto, auth.getName()));
    }
}
