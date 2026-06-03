package explora.map.service;

import explora.map.dto.JwtResponseDTO;
import explora.map.entity.RefreshToken;
import explora.map.entity.Usuaria;

/** Interface do servizo de xestión de refresh tokens JWT. */
public interface RefreshTokenService {
    /**
     * Resultado da rotación de token: novo access token DTO (para o corpo JSON) e hash
     * do novo refresh token (para emitir como cookie HttpOnly no controller).
     */
    record RefreshResult(JwtResponseDTO jwtResponse, String refreshTokenHash) {}

    RefreshToken novo(Usuaria usuaria);
    void revoke(String token);
    void revokeAllUserTokens(Usuaria usuaria);
    /** Rota o par de tokens a partir do hash do refresh token actual. */
    RefreshResult refresh(String refreshTokenHash);
    RefreshToken verifyExpiration(RefreshToken token);
}
