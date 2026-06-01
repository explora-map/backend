package explora.map.controller;

import explora.map.dto.CategoriaRequestDTO;
import explora.map.dto.CategoriaResponseDTO;
import explora.map.service.CategoriaService;
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

/** Controlador REST para categorías dun mapa. Endpoints baixo /api/mapas/{mapaId}/categorias e /api/categorias. */
@Tag(name = "Categorías", description = "Xestión de categorías de marcadores dentro dun mapa")
@RestController
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Operation(summary = "Listar as categorías dun mapa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de categorías do mapa"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para ver este mapa privado"),
        @ApiResponse(responseCode = "404", description = "Mapa non atopado")
    })
    @GetMapping("/api/mapas/{mapaId}/categorias")
    public ResponseEntity<List<CategoriaResponseDTO>> listarPorMapa(@PathVariable Long mapaId, Authentication auth) {
        String username = (auth != null) ? auth.getName() : null;
        return ResponseEntity.ok(categoriaService.listarPorMapa(mapaId, username));
    }

    @Operation(summary = "Crear unha nova categoría nun mapa")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Categoría creada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos da categoría inválidos"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para crear categorías neste mapa")
    })
    @PostMapping("/api/mapas/{mapaId}/categorias")
    public ResponseEntity<CategoriaResponseDTO> crear(@PathVariable Long mapaId, @Valid @RequestBody CategoriaRequestDTO dto, Authentication auth) {
        return ResponseEntity.status(201).body(categoriaService.crear(mapaId, dto, auth.getName()));
    }

    @Operation(summary = "Editar unha categoría existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de edición inválidos"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para editar esta categoría"),
        @ApiResponse(responseCode = "404", description = "Categoría non atopada")
    })
    @PutMapping("/api/categorias/{id}")
    public ResponseEntity<CategoriaResponseDTO> editar(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO dto, Authentication auth) {
        return ResponseEntity.ok(categoriaService.editar(id, dto, auth.getName()));
    }

    @Operation(summary = "Eliminar unha categoría e desasociar os seus marcadores")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Categoría eliminada correctamente"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para eliminar esta categoría"),
        @ApiResponse(responseCode = "404", description = "Categoría non atopada")
    })
    @DeleteMapping("/api/categorias/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        categoriaService.eliminar(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
