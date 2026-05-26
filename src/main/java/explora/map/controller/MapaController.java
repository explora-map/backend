package explora.map.controller;

import explora.map.dto.MapaColaboracionResponseDTO;
import explora.map.dto.MapaResponseDTO;
import explora.map.dto.MapaRequestDTO;
import explora.map.entity.TipoMapa;
import explora.map.service.MapaService;
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
import java.util.Map;

/** Controlador REST para a creación, consulta, edición e eliminación de mapas. */
@Tag(name = "Mapas", description = "Creación, consulta, edición e eliminación de mapas colaborativos")
@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
public class MapaController {
    private final MapaService mapaService;

    // TODO(Sprint 4 — REST cleanup): URL verbs (/novo, /editar/{id}, /eliminar/{id}) violate
    // RESTful conventions. Preferred: POST /api/mapas, PUT /api/mapas/{id},
    // DELETE /api/mapas/{id}. Not changed now to avoid breaking the frontend.
    @Operation(summary = "Crear un novo mapa colaborativo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Mapa creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos do mapa inválidos"),
        @ApiResponse(responseCode = "401", description = "Non autenticada")
    })
    @PostMapping("/novo")
    public ResponseEntity<MapaResponseDTO> novoMapa(@Valid @RequestBody MapaRequestDTO mapaRequest, Authentication auth) {
        return ResponseEntity.ok(
                mapaService.novo(auth.getName(), mapaRequest)
        );
    }

    @Operation(summary = "Listar os mapas nos que a usuaria autenticada participa como colaboradora")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de mapas de colaboración con rol"),
        @ApiResponse(responseCode = "401", description = "Non autenticada")
    })
    @GetMapping("/colaboracións")
    public ResponseEntity<List<MapaColaboracionResponseDTO>> obterMapasColaboradora(Authentication auth) {
        return ResponseEntity.ok(mapaService.obterMapasColaboradora(auth.getName()));
    }

    @Operation(summary = "Obter un mapa polo seu identificador")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Datos do mapa solicitado"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para ver este mapa privado"),
        @ApiResponse(responseCode = "404", description = "Mapa non atopado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MapaResponseDTO> obterMapaPorId(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(mapaService.obterPorId(id, auth.getName()));
    }

    @Operation(summary = "Listar os mapas creados pola usuaria autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de mapas propios da usuaria"),
        @ApiResponse(responseCode = "401", description = "Non autenticada")
    })
    @GetMapping("/meus")
    public ResponseEntity<List<MapaResponseDTO>> obterMapaPorUsername(Authentication auth) {
        return ResponseEntity.ok(mapaService.obterPorUsername(auth.getName()));
    }

    @Operation(summary = "Editar os datos dun mapa existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mapa actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de edición inválidos"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para editar este mapa"),
        @ApiResponse(responseCode = "404", description = "Mapa non atopado")
    })
    @PutMapping("/editar/{id}")
    public ResponseEntity<MapaResponseDTO> editarMapa(@PathVariable Long id, @Valid @RequestBody MapaRequestDTO mapaRequest, Authentication auth) {
        return ResponseEntity.ok(mapaService.editar(id, auth.getName(), mapaRequest));
    }

    @Operation(summary = "Eliminar un mapa e todo o seu contido en cascada")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Mapa eliminado correctamente"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para eliminar este mapa"),
        @ApiResponse(responseCode = "404", description = "Mapa non atopado")
    })
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarMapa(@PathVariable Long id, Authentication auth) {
        mapaService.eliminar(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar mapas públicos dentro dun radio xeográfico dado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de mapas públicos dentro do radio"),
        @ApiResponse(responseCode = "401", description = "Non autenticada")
    })
    @GetMapping("/publicos")
    public ResponseEntity<List<MapaResponseDTO>> obterPorTipoPublico(@RequestParam Double latitude, @RequestParam Double lonxitude, @RequestParam(defaultValue = "5") Double radio) {
        return ResponseEntity.ok(mapaService.obterPorTipoPublico(latitude, lonxitude, radio));
    }

    @Operation(summary = "Cambiar a visibilidade dun mapa entre PUBLICO e PRIVADO")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Visibilidade cambiada correctamente"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para cambiar a visibilidade deste mapa"),
        @ApiResponse(responseCode = "404", description = "Mapa non atopado")
    })
    @PatchMapping("/{id}/visibilidade")
    public ResponseEntity<MapaResponseDTO> cambiarVisibilidade(@PathVariable Long id, @RequestBody Map<String, TipoMapa> body, Authentication auth) {
        return ResponseEntity.ok(mapaService.cambiarVisibilidade(id, auth.getName(), body.get("tipo")));
    }
}
