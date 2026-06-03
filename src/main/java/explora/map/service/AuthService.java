package explora.map.service;

import explora.map.dto.JwtResponseDTO;
import explora.map.dto.LoginRequestDTO;
import explora.map.dto.RegisterRequestDTO;

/** Interface do servizo de autenticación: rexistro, login e verificación de conta. */
public interface AuthService {
    /**
     * Resultado do login: access token DTO (para o corpo JSON) e hash do refresh token
     * (para emitir como cookie HttpOnly no controller).
     */
    record LoginResult(JwtResponseDTO jwtResponse, String refreshTokenHash) {}

    void rexistro(RegisterRequestDTO request);
    LoginResult entrar(LoginRequestDTO request);
    /** Revoga o refresh token identificado polo seu hash. */
    void sair(String refreshToken);
}
