package explora.map.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/** DTO de entrada co refresh token para renovar ou pechar a sesión. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequestDTO {
    @NotBlank
    private String refreshToken;
}
