package explora.map.controller;

import explora.map.dto.JwtResponseDTO;
import explora.map.dto.LoginRequestDTO;
import explora.map.dto.RegisterRequestDTO;
import explora.map.service.AuthService;
import explora.map.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/** Controlador REST para autenticación. Endpoints baixo /api/auth. */
@Tag(name = "Autenticación", description = "Rexistro, login, renovación de token e verificación de conta")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

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
     * Autentica as credenciais da usuaria e devolve un access token JWT.
     *
     * <p>O access token ten unha duración curta (configurada en
     * {@code jwt.access.expiracion}). O refresh token emítese como cookie HttpOnly
     * co path {@code /api/auth} para que só os endpoints de renovación e peche
     * o reciban automaticamente.</p>
     *
     * @param request credenciais de acceso: username e contrasinal
     * @param response resposta HTTP onde se engade a cookie {@code refresh_token}
     * @return {@code 200 OK} con {@link JwtResponseDTO} que contén o access token,
     *         o tipo ({@code Bearer}) e a data de expiración
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         se o username non existe ou o contrasinal é incorrecto
     *         ({@code GlobalExceptionHandler} → 401)
     * @throws IllegalStateException se a conta existe pero aínda non foi verificada
     *                               por correo electrónico ({@code GlobalExceptionHandler} → 403)
     */
    @Operation(summary = "Iniciar sesión e obter tokens JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login correcto; devolve access token; refresh token en cookie HttpOnly"),
        @ApiResponse(responseCode = "401", description = "Credenciais incorrectas"),
        @ApiResponse(responseCode = "403", description = "Conta non verificada por correo electrónico")
    })
    @PostMapping("/entrar")
    public ResponseEntity<JwtResponseDTO> authenticateUser(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response) {
        AuthService.LoginResult result = authService.entrar(request);
        addRefreshCookie(response, result.refreshTokenHash());
        return ResponseEntity.ok(result.jwtResponse());
    }

    /**
     * Rota o par de tokens: le o refresh token da cookie, invalida o token actual
     * e emite un novo par (novo access token + nova cookie refresh).
     *
     * <p>Implementa rotación de refresh token: o token recibido queda revogado
     * e xéranse un novo access token e un novo refresh token. Isto limita a
     * fiestra de reutilización en caso de roubo do token.</p>
     *
     * @param refreshToken hash do refresh token activo, lido da cookie {@code refresh_token}
     * @param response     resposta HTTP onde se engade a nova cookie {@code refresh_token}
     * @return {@code 200 OK} con {@link JwtResponseDTO} co novo access token,
     *         ou {@code 401} se a cookie non está presente ou o token é inválido
     */
    @Operation(summary = "Renovar o par de tokens JWT mediante refresh token en cookie")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Novo par de tokens emitido correctamente"),
        @ApiResponse(responseCode = "401", description = "Cookie refresh_token ausente, inválida ou expirada")
    })
    @PostMapping("/renovar")
    public ResponseEntity<JwtResponseDTO> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RefreshTokenService.RefreshResult result = refreshTokenService.refresh(refreshToken);
        addRefreshCookie(response, result.refreshTokenHash());
        return ResponseEntity.ok(result.jwtResponse());
    }

    /**
     * Pecha a sesión da usuaria revogando o refresh token activo e invalidando a cookie.
     *
     * <p>O access token existente non se invalida de forma explícita
     * (non hai lista negra), pero expirará ao cabo do seu período configurado.
     * Se a cookie non está presente, a operación responde {@code 200} igualmente
     * (idempotente).</p>
     *
     * @param refreshToken hash do refresh token activo, lido da cookie {@code refresh_token}
     * @param response     resposta HTTP onde se engade a cookie de borrado (Max-Age=0)
     * @return {@code 200 OK} con mensaxe de confirmación do peche de sesión
     */
    @Operation(summary = "Pechar sesión e revogar o refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sesión pechada correctamente")
    })
    @PostMapping("/pechar")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        clearRefreshCookie(response);
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.sair(refreshToken);
        }
        return ResponseEntity.ok("Sesión pechada correctamente");
    }

    // ── Utilidades privadas de cookie ────────────────────────────────────────

    private void addRefreshCookie(HttpServletResponse response, String tokenHash) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokenHash)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
