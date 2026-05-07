package explora.map.controller;

import explora.map.entity.TokenVerificacion;
import explora.map.entity.Usuaria;
import explora.map.repository.TokenVerificacionRepository;
import explora.map.repository.UsuariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerificacionController {

    private final TokenVerificacionRepository tokenVerificacionRepository;
    private final UsuariaRepository usuariaRepository;

    // Verify email token — called from frontend after clicking link
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
