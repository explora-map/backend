package explora.map.controller;

import explora.map.entity.TokenVerificacion;
import explora.map.entity.Usuaria;
import explora.map.repository.TokenVerificacionRepository;
import explora.map.repository.UsuariaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

/** Controlador REST para a verificación de correo electrónico mediante token. */
@Tag(name = "Verificación", description = "Verificación de correo electrónico mediante token")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerificacionController {

    private final TokenVerificacionRepository tokenVerificacionRepository;
    private final UsuariaRepository usuariaRepository;

    // Verify email token — called from frontend after clicking link
    @Operation(summary = "Verificar o correo electrónico mediante token enviado por email")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conta verificada correctamente"),
        @ApiResponse(responseCode = "400", description = "Token inválido, xa usado ou caducado")
    })
    @GetMapping("/verificar")
    @Transactional
    public ResponseEntity<Map<String, String>> verificar(@RequestParam String token) {
        TokenVerificacion tv = tokenVerificacionRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Token de verificación non válido."));

        if (tv.isUsado()) {
            throw new IllegalStateException("Este token xa foi usado.");
        }

        // Add debug log to diagnose expiry issues
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime expiracion = tv.getDataExpiracion();

        if (expiracion.isBefore(agora)) {
            throw new IllegalStateException(
                "O token de verificación caducou. Expiraba: " + expiracion + ", Agora: " + agora
            );
        }

        Usuaria usuaria = tv.getUsuaria();
        usuaria.setVerificada(true);
        usuariaRepository.save(usuaria);

        tv.setUsado(true);
        tokenVerificacionRepository.save(tv);

        return ResponseEntity.ok(Map.of("message", "Conta verificada correctamente. Xa podes iniciar sesión."));
    }
}
