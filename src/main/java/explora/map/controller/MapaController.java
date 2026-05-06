package explora.map.controller;

import explora.map.dto.MapaResponseDTO;
import explora.map.dto.MapaRequestDTO;
import explora.map.entity.TipoMapa;
import explora.map.service.MapaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
public class MapaController {
    private final MapaService mapaService;

    // TODO(Sprint 4 — REST cleanup): URL verbs (/novo, /editar/{id}, /eliminar/{id}) violate
    // RESTful conventions. Preferred: POST /api/mapas, PUT /api/mapas/{id},
    // DELETE /api/mapas/{id}. Not changed now to avoid breaking the frontend.
    @PostMapping("/novo")
    public ResponseEntity<MapaResponseDTO> novoMapa(@Valid @RequestBody MapaRequestDTO mapaRequest, Authentication auth) {
        return ResponseEntity.ok(
                mapaService.novo(auth.getName(), mapaRequest)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MapaResponseDTO> obterMapaPorId(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(mapaService.obterPorId(id, auth.getName()));
    }

    @GetMapping("/meus")
    public ResponseEntity<List<MapaResponseDTO>> obterMapaPorUsername(Authentication auth) {
        return ResponseEntity.ok(mapaService.obterPorUsername(auth.getName()));
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<MapaResponseDTO> editarMapa(@PathVariable Long id, @Valid @RequestBody MapaRequestDTO mapaRequest, Authentication auth) {
        return ResponseEntity.ok(mapaService.editar(id, auth.getName(), mapaRequest));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarMapa(@PathVariable Long id, Authentication auth) {
        mapaService.eliminar(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/publicos")
    public ResponseEntity<List<MapaResponseDTO>> obterPorTipoPublico(@RequestParam Double latitude, @RequestParam Double lonxitude, @RequestParam(defaultValue = "5") Double radio) {
        return ResponseEntity.ok(mapaService.obterPorTipoPublico(latitude, lonxitude, radio));
    }

    @PatchMapping("/{id}/visibilidade")
    public ResponseEntity<MapaResponseDTO> cambiarVisibilidade(@PathVariable Long id, @RequestBody Map<String, TipoMapa> body, Authentication auth) {
        return ResponseEntity.ok(mapaService.cambiarVisibilidade(id, auth.getName(), body.get("tipo")));
    }
}
