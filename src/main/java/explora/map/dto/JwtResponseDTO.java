package explora.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/** DTO de saída coas credenciais JWT tras un login ou renovación de token. */
@Schema(description = "Par de tokens JWT devolvido tras o login ou a renovación de token")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponseDTO {
    @Schema(description = "Access token JWT de curta duración para autenticar peticións", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
    @Schema(description = "Refresh token de longa duración para renovar o access token", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;
    // "Bearer" — inclúese para que o frontend poida usalo directamente
    // no header: Authorization: Bearer <accessToken>
    @Schema(description = "Tipo de token; sempre Bearer", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";
    @Schema(description = "Data e hora de expiración do access token", example = "2026-04-15T10:30:00")
    private LocalDateTime tokenExpiration;
}
