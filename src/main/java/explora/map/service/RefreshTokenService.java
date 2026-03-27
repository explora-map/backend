package explora.map.service;

import explora.map.dto.JwtResponseDTO;
import explora.map.dto.RefreshTokenRequestDTO;
import explora.map.entity.RefreshToken;
import explora.map.entity.Usuaria;

public interface RefreshTokenService {
    RefreshToken novo(Usuaria usuaria);
    void revoke(String token);
    void revokeAllUserTokens(Usuaria usuaria);
    JwtResponseDTO refresh(RefreshTokenRequestDTO request);
    RefreshToken verifyExpiration(RefreshToken token);

}
