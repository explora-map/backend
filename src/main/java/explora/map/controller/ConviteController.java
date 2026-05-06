package explora.map.controller;

import explora.map.dto.ConviteResponseDTO;
import explora.map.dto.ConviteRequestDTO;
import explora.map.service.ConviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/convites")
@RequiredArgsConstructor
public class ConviteController {
    private final ConviteService conviteService;

    @PostMapping("/novo")
    public ResponseEntity<ConviteResponseDTO> novoConvite(@Valid @RequestBody ConviteRequestDTO conviteRequest, Authentication auth) {
        return ResponseEntity.ok(conviteService.novo(auth.getName(), conviteRequest));
    }

    @GetMapping
    public ResponseEntity<List<ConviteResponseDTO>> obterConvitePorUsername(Authentication auth) {
        return ResponseEntity.ok(conviteService.obterPorUsername(auth.getName()));
    }

    @GetMapping("/recibidos")
    public ResponseEntity<List<ConviteResponseDTO>> obterConvitesRecibidos(Authentication auth) {
        return ResponseEntity.ok(conviteService.obterRecibidos(auth.getName()));
    }

    @PatchMapping("/{token}/aceptar")
    public ResponseEntity<Void> aceptarConvite(@PathVariable UUID token, Authentication auth) {
        conviteService.aceptar(token, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{token}/rexeitar")
    public ResponseEntity<Void> rexeitarConvite(@PathVariable UUID token, Authentication auth) {
        conviteService.rexeitar(token, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> cancelarConvite(@PathVariable UUID token, Authentication auth) {
        conviteService.cancelar(token, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
