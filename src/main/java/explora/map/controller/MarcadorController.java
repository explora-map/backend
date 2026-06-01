package explora.map.controller;

import explora.map.dto.MarcadorRequestDTO;
import explora.map.dto.MarcadorResponseDTO;
import explora.map.service.MarcadorService;
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

/** Controlador REST para marcadores xeográficos. Endpoints baixo /api/mapas/{mapaId}/marcadores e /api/marcadores. */
@Tag(name = "Marcadores", description = "Creación, consulta, edición e eliminación de marcadores xeográficos nun mapa")
@RestController
@RequiredArgsConstructor
public class MarcadorController {

    private final MarcadorService marcadorService;

    @Operation(summary = "Listar os marcadores dun mapa", tags = {"Marcadores"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de marcadores do mapa"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para ver este mapa privado"),
        @ApiResponse(responseCode = "404", description = "Mapa non atopado")
    })
    @GetMapping("/api/mapas/{mapaId}/marcadores")
    public ResponseEntity<List<MarcadorResponseDTO>> listarPorMapa(@PathVariable Long mapaId, Authentication auth) {
        String username = (auth != null) ? auth.getName() : null;
        return ResponseEntity.ok(marcadorService.listarPorMapa(mapaId, username));
    }

    @Operation(summary = "Crear un novo marcador nun mapa", tags = {"Marcadores"})
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Marcador creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos do marcador inválidos"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para crear marcadores neste mapa")
    })
    @PostMapping("/api/mapas/{mapaId}/marcadores")
    public ResponseEntity<MarcadorResponseDTO> crear(@PathVariable Long mapaId, @Valid @RequestBody MarcadorRequestDTO dto, Authentication auth) {
        return ResponseEntity.status(201).body(marcadorService.crear(mapaId, dto, auth.getName()));
    }

    @Operation(summary = "Editar un marcador existente", tags = {"Marcadores"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Marcador actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de edición inválidos"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para editar este marcador"),
        @ApiResponse(responseCode = "404", description = "Marcador non atopado")
    })
    @PutMapping("/api/marcadores/{id}")
    public ResponseEntity<MarcadorResponseDTO> editar(@PathVariable Long id, @Valid @RequestBody MarcadorRequestDTO dto, Authentication auth) {
        return ResponseEntity.ok(marcadorService.editar(id, dto, auth.getName()));
    }

    @Operation(summary = "Eliminar un marcador", tags = {"Marcadores"})
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Marcador eliminado correctamente"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para eliminar este marcador"),
        @ApiResponse(responseCode = "404", description = "Marcador non atopado")
    })
    @DeleteMapping("/api/marcadores/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        marcadorService.eliminar(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
