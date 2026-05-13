package explora.map.controller;

import explora.map.dto.MapaResponseDTO;
import explora.map.service.MapaGardadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MapaGardadoController {

    private final MapaGardadoService mapaGardadoService;

    @GetMapping("/api/mapas/gardados")
    public ResponseEntity<List<MapaResponseDTO>> obterMapasGardados(Authentication auth) {
        return ResponseEntity.ok(mapaGardadoService.obterMapasGardados(auth.getName()));
    }

    @PostMapping("/api/mapas/{mapaId}/gardar")
    public ResponseEntity<Void> gardarMapa(@PathVariable Long mapaId, Authentication auth) {
        mapaGardadoService.gardarMapa(mapaId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/mapas/{mapaId}/gardar")
    public ResponseEntity<Void> desgardarMapa(@PathVariable Long mapaId, Authentication auth) {
        mapaGardadoService.desgardarMapa(mapaId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
