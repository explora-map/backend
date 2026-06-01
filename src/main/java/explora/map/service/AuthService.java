package explora.map.service;

import explora.map.dto.JwtResponseDTO;
import explora.map.dto.LoginRequestDTO;
import explora.map.dto.RefreshTokenRequestDTO;
import explora.map.dto.RegisterRequestDTO;

/** Interface do servizo de autenticación: rexistro, login e verificación de conta. */
public interface AuthService {
    void rexistro(RegisterRequestDTO request);
    JwtResponseDTO entrar(LoginRequestDTO request);
    void sair(RefreshTokenRequestDTO request);
}
