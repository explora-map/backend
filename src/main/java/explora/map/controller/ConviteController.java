package explora.map.controller;

import explora.map.dto.ConviteResponseDTO;
import explora.map.dto.ConviteRequestDTO;
import explora.map.service.ConviteService;
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
import java.util.UUID;

/** Controlador REST para convites entre usuarias. Endpoints baixo /api/convites. */
@Tag(name = "Convites", description = "Envío, consulta e xestión de convites a mapas privados")
@RestController
@RequestMapping("/api/convites")
@RequiredArgsConstructor
public class ConviteController {
    private final ConviteService conviteService;

    @Operation(summary = "Enviar un convite a outra usuaria para unirse a un mapa")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Convite enviado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos do convite inválidos"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "404", description = "Mapa ou usuaria convidada non atopada"),
        @ApiResponse(responseCode = "409", description = "Xa existe un convite pendente para esta usuaria neste mapa")
    })
    @PostMapping("/novo")
    public ResponseEntity<ConviteResponseDTO> novoConvite(@Valid @RequestBody ConviteRequestDTO conviteRequest, Authentication auth) {
        return ResponseEntity.ok(conviteService.novo(auth.getName(), conviteRequest));
    }

    @Operation(summary = "Listar os convites enviados pola usuaria autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de convites enviados"),
        @ApiResponse(responseCode = "401", description = "Non autenticada")
    })
    @GetMapping
    public ResponseEntity<List<ConviteResponseDTO>> obterConvitePorUsername(Authentication auth) {
        return ResponseEntity.ok(conviteService.obterPorUsername(auth.getName()));
    }

    @Operation(summary = "Listar os convites recibidos pola usuaria autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de convites recibidos"),
        @ApiResponse(responseCode = "401", description = "Non autenticada")
    })
    @GetMapping("/recibidos")
    public ResponseEntity<List<ConviteResponseDTO>> obterConvitesRecibidos(Authentication auth) {
        return ResponseEntity.ok(conviteService.obterRecibidos(auth.getName()));
    }

    @Operation(summary = "Aceptar un convite e unirse ao mapa como membro")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Convite aceptado; usuaria engadida como membro"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "404", description = "Convite non atopado")
    })
    @PatchMapping("/{token}/aceptar")
    public ResponseEntity<Void> aceptarConvite(@PathVariable UUID token, Authentication auth) {
        conviteService.aceptar(token, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rexeitar un convite recibido")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Convite rexeitado correctamente"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "404", description = "Convite non atopado")
    })
    @PatchMapping("/{token}/rexeitar")
    public ResponseEntity<Void> rexeitarConvite(@PathVariable UUID token, Authentication auth) {
        conviteService.rexeitar(token, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancelar un convite enviado pola usuaria autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Convite cancelado correctamente"),
        @ApiResponse(responseCode = "401", description = "Non autenticada"),
        @ApiResponse(responseCode = "403", description = "Sen permiso para cancelar este convite"),
        @ApiResponse(responseCode = "404", description = "Convite non atopado")
    })
    @DeleteMapping("/{token}")
    public ResponseEntity<Void> cancelarConvite(@PathVariable UUID token, Authentication auth) {
        conviteService.cancelar(token, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
