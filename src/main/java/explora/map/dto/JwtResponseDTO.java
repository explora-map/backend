package explora.map.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponseDTO {
    private String accessToken;
    private String refreshToken;
    // "Bearer" — inclúese para que o frontend poida usalo directamente
    // no header: Authorization: Bearer <accessToken>
    @Builder.Default
    private String tokenType = "Bearer";
    private LocalDateTime tokenExpiration;
}
