package explora.map.dto;

import explora.map.entity.RolMapa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/** DTO de entrada para enviar un convite a unha usuaria. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConviteRequestDTO {
    @NotNull
    private Long mapaId;
    @NotBlank
    private String usernameConvidada;
    private RolMapa rol;
}
