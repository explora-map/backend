package explora.map.service;

import explora.map.dto.JwtResponseDTO;
import explora.map.dto.RefreshTokenRequestDTO;
import explora.map.entity.RefreshToken;
import explora.map.entity.Usuaria;
import explora.map.repository.RefreshTokenRepository;
import explora.map.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh.expiration.days}")
    private long refreshExpirationDays;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;

    /**
     * Crea (ou substitúe) un refresh token para o usuario dado.
     * Un usuario só pode ter un refresh token activo á vez —
     * o anterior bórrase antes de crear o novo.
     */
    @Transactional
    @Override
    public RefreshToken novo(Usuaria usuaria) {
        revokeAllUserTokens(usuaria);

        RefreshToken refreshToken = RefreshToken.builder()
                .usuaria(usuaria)
                // UUID como valor do token — opaco, sen información
                .tokenHash(UUID.randomUUID().toString())
                .dataExpiracion(LocalDateTime.now().plusSeconds(refreshExpirationDays * 24 * 3600))
                .isRevoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verifica se o refresh token expirou.
     * Se expirou, bórrao e lanza excepción.
     */
    @Transactional
    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getDataExpiracion().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException(
                    "O refresh token expirou. Por favor, inicia sesión de novo."
            );
        }
        return token;
    }


    @Transactional
    @Override
    public JwtResponseDTO refresh(RefreshTokenRequestDTO request) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token non válido ou inexistente"));

        verifyExpiration(stored);

        Usuaria usuaria = stored.getUsuaria();

        // Rotación: bórrase o token anterior e xérase un novo par
        String newAccessToken = jwtUtils.generateTokenFromUsername(usuaria.getUsername());
        RefreshToken newRefresh = novo(usuaria);

        return JwtResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefresh.getTokenHash())
                .tokenType("Bearer")
                .tokenExpiration(jwtUtils.getExpirationDateTime(newAccessToken))
                .build();
    }

    @Override
    public void revoke(String refreshToken) {
        refreshTokenRepository.findByTokenHash(refreshToken)
                .ifPresent(t -> {
                    t.setIsRevoked(true);
                    refreshTokenRepository.save(t);
                });
    }

    @Override
    public void revokeAllUserTokens(Usuaria usuaria) {
        refreshTokenRepository.findByUsuariaAndIsRevokedFalse(usuaria)
                .forEach(t -> {
                    t.setIsRevoked(true);
                    refreshTokenRepository.save(t);
                });
    }

}

