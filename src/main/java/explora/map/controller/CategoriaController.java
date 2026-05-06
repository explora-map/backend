package explora.map.controller;

import explora.map.dto.CategoriaRequestDTO;
import explora.map.dto.CategoriaResponseDTO;
import explora.map.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping("/api/mapas/{mapaId}/categorias")
    public ResponseEntity<List<CategoriaResponseDTO>> listarPorMapa(@PathVariable Long mapaId, Authentication auth) {
        return ResponseEntity.ok(categoriaService.listarPorMapa(mapaId, auth.getName()));
    }

    @PostMapping("/api/mapas/{mapaId}/categorias")
    public ResponseEntity<CategoriaResponseDTO> crear(@PathVariable Long mapaId, @Valid @RequestBody CategoriaRequestDTO dto, Authentication auth) {
        return ResponseEntity.status(201).body(categoriaService.crear(mapaId, dto, auth.getName()));
    }

    @PutMapping("/api/categorias/{id}")
    public ResponseEntity<CategoriaResponseDTO> editar(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO dto, Authentication auth) {
        return ResponseEntity.ok(categoriaService.editar(id, dto, auth.getName()));
    }

    @DeleteMapping("/api/categorias/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        categoriaService.eliminar(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
