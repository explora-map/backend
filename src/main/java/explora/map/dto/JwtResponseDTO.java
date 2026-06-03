package explora.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/** DTO de saída co access token JWT tras un login ou renovación de token. O refresh token emítese como cookie HttpOnly. */
@Schema(description = "Access token JWT devolvido tras o login ou a renovación de token")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponseDTO {
    @Schema(description = "Access token JWT de curta duración para autenticar peticións", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
    // "Bearer" — inclúese para que o frontend poida usalo directamente
    // no header: Authorization: Bearer <accessToken>
    @Schema(description = "Tipo de token; sempre Bearer", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";
    @Schema(description = "Data e hora de expiración do access token", example = "2026-04-15T10:30:00")
    private LocalDateTime tokenExpiration;
    @Schema(description = "Nome de usuaria autenticada", example = "maria_lopez")
    private String username;
}
