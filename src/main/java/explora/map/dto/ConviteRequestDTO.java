package explora.map.dto;

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
}
