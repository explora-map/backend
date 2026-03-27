package explora.map.controller;

import explora.map.dto.JwtResponseDTO;
import explora.map.dto.LoginRequestDTO;
import explora.map.dto.RefreshTokenRequestDTO;
import explora.map.dto.RegisterRequestDTO;
import explora.map.service.AuthService;
import explora.map.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/rexistro")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO request){
        authService.rexistro(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/entrar")
    public ResponseEntity<JwtResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO request){
        return ResponseEntity.ok(authService.entrar(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(refreshTokenService.refresh(request));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        authService.sair(request);
        return ResponseEntity.ok("Sesión pechada correctaente");
    }
}
