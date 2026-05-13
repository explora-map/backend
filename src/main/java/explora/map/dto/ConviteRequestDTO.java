package explora.map.dto;

import explora.map.entity.RolMapa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
