package explora.map.controller;

import explora.map.dto.PerfilRequestDTO;
import explora.map.dto.PerfilResponseDTO;
import explora.map.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Controlador REST para a consulta e xestión do perfil da usuaria autenticada. */
@Tag(name = "Perfil", description = "Consulta e actualización do perfil da usuaria autenticada")
@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @Operation(summary = "Obter o perfil da usuaria autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil da usuaria"),
        @ApiResponse(responseCode = "401", description = "Non autenticada")
    })
    @GetMapping
    public ResponseEntity<PerfilResponseDTO> obterPerfil(Authentication authentication) {
        return ResponseEntity.ok(perfilService.obterPerfil(authentication.getName()));
    }

    @Operation(summary = "Actualizar os datos do perfil da usuaria autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "409", description = "Username ou correo xa están en uso por outra usuaria")
    })
    @PatchMapping
    public ResponseEntity<PerfilResponseDTO> actualizarPerfil(
            @Valid @RequestBody PerfilRequestDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(perfilService.actualizarPerfil(authentication.getName(), dto));
    }

    @Operation(summary = "Eliminar permanentemente a conta da usuaria autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Conta eliminada correctamente"),
        @ApiResponse(responseCode = "401", description = "Non autenticada")
    })
    @DeleteMapping
    public ResponseEntity<Void> eliminarConta(Authentication authentication) {
        perfilService.eliminar(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
