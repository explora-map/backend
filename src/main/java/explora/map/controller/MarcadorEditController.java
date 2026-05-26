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

@Tag(name = "Edición de marcadores", description = "Modificación e eliminación de marcadores xeográficos")
@RestController
@RequestMapping("/api/marcadores")
@RequiredArgsConstructor
public class MarcadorEditController {

    private final MarcadorService marcadorService;

    @Operation(summary = "Editar un marcador existente", tags = {"Edición de marcadores"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Marcador actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de edición inválidos"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para editar este marcador"),
        @ApiResponse(responseCode = "404", description = "Marcador non atopado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MarcadorResponseDTO> editar(@PathVariable Long id, @Valid @RequestBody MarcadorRequestDTO dto, Authentication auth) {
        return ResponseEntity.ok(marcadorService.editar(id, dto, auth.getName()));
    }

    @Operation(summary = "Eliminar un marcador", tags = {"Edición de marcadores"})
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Marcador eliminado correctamente"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para eliminar este marcador"),
        @ApiResponse(responseCode = "404", description = "Marcador non atopado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        marcadorService.eliminar(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
