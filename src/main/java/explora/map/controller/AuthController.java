package explora.map.controller;

import explora.map.dto.JwtResponseDTO;
import explora.map.dto.LoginRequestDTO;
import explora.map.dto.RefreshTokenRequestDTO;
import explora.map.dto.RegisterRequestDTO;
import explora.map.service.AuthService;
import explora.map.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controlador REST para autenticación. Endpoints baixo /api/auth. */
@Tag(name = "Autenticación", description = "Rexistro, login, renovación de token e verificación de conta")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Rexistra unha nova conta de usuaria e envía un correo de verificación.
     *
     * <p>O rexistro créase en estado non verificado. A usuaria debe confirmar
     * o seu correo electrónico antes de poder iniciar sesión.</p>
     *
     * @param request datos de rexistro: nome, username, correo e contrasinal
     * @return {@code 200 OK} sen corpo se o rexistro se completou con éxito
     * @throws IllegalArgumentException se o username ou o correo xa están en uso
     *                                  ({@code GlobalExceptionHandler} → 404)
     */
    @Operation(summary = "Rexistrar nova conta de usuaria")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Conta creada; correo de verificación enviado"),
        @ApiResponse(responseCode = "400", description = "Datos de rexistro inválidos"),
        @ApiResponse(responseCode = "409", description = "Username ou correo xa están en uso")
    })
    @PostMapping("/rexistro")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO request){
        authService.rexistro(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Autentica as credenciais da usuaria e devolve un par de tokens JWT.
     *
     * <p>O access token ten unha duración curta (configurada en
     * {@code jwt.access.expiracion}). O refresh token permite renovalo
     * sen necesidade de volver a introducir as credenciais.</p>
     *
     * @param request credenciais de acceso: username e contrasinal
     * @return {@code 200 OK} con {@link JwtResponseDTO} que contén o access token,
     *         o refresh token, o tipo ({@code Bearer}) e a data de expiración
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         se o username non existe ou o contrasinal é incorrecto
     *         ({@code GlobalExceptionHandler} → 401)
     * @throws IllegalStateException se a conta existe pero aínda non foi verificada
     *                               por correo electrónico ({@code GlobalExceptionHandler} → 403)
     */
    @Operation(summary = "Iniciar sesión e obter tokens JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login correcto; devolve access token e refresh token"),
        @ApiResponse(responseCode = "401", description = "Credenciais incorrectas"),
        @ApiResponse(responseCode = "403", description = "Conta non verificada por correo electrónico")
    })
    @PostMapping("/entrar")
    public ResponseEntity<JwtResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO request){
        return ResponseEntity.ok(authService.entrar(request));
    }

    /**
     * Rota o par de tokens: invalida o refresh token actual e emite un novo par.
     *
     * <p>Implementa rotación de refresh token: o token enviado queda revogado
     * e xéranse un novo access token e un novo refresh token. Isto limita a
     * fiestra de reutilización en caso de roubo do token.</p>
     *
     * @param request corpo coa propiedade {@code refreshToken} (hash UUID do token activo)
     * @return {@code 200 OK} con {@link JwtResponseDTO} co novo par de tokens
     * @throws RuntimeException se o refresh token non existe ou xa expirou
     *                          ({@code GlobalExceptionHandler} → 500)
     */
    @Operation(summary = "Renovar o par de tokens JWT mediante refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Novo par de tokens emitido correctamente"),
        @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    })
    @PostMapping("/renovar")
    public ResponseEntity<JwtResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(refreshTokenService.refresh(request));
    }


    /**
     * Pecha a sesión da usuaria revogando o refresh token activo.
     *
     * <p>O access token existente non se invalida de forma explícita
     * (non hai lista negra), pero expirará ao cabo do seu período configurado.
     * Se o token enviado non existe, a operación non fai nada (idempotente).</p>
     *
     * @param request corpo coa propiedade {@code refreshToken} a revogar
     * @return {@code 200 OK} con mensaxe de confirmación do peche de sesión
     */
    @Operation(summary = "Pechar sesión e revogar o refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sesión pechada correctamente"),
        @ApiResponse(responseCode = "400", description = "Corpo da petición inválido")
    })
    @PostMapping("/pechar")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        authService.sair(request);
        return ResponseEntity.ok("Sesión pechada correctaente");
    }
}
