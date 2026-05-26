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

/** Controlador REST para a creación e consulta de marcadores xeográficos. */
@Tag(name = "Marcadores", description = "Creación e consulta de marcadores xeográficos nun mapa")
@RestController
@RequestMapping("/api/mapas")
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
    @GetMapping("/{mapaId}/marcadores")
    public ResponseEntity<List<MarcadorResponseDTO>> listarPorMapa(@PathVariable Long mapaId, Authentication auth) {
        return ResponseEntity.ok(marcadorService.listarPorMapa(mapaId, auth.getName()));
    }

    @Operation(summary = "Crear un novo marcador nun mapa", tags = {"Marcadores"})
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Marcador creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos do marcador inválidos"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para crear marcadores neste mapa")
    })
    @PostMapping("/{mapaId}/marcadores")
    public ResponseEntity<MarcadorResponseDTO> crear(@PathVariable Long mapaId, @Valid @RequestBody MarcadorRequestDTO dto, Authentication auth) {
        return ResponseEntity.status(201).body(marcadorService.crear(mapaId, dto, auth.getName()));
    }

}
