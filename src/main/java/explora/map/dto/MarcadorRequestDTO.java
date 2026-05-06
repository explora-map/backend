package explora.map.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarcadorRequestDTO {

    @NotBlank
    private String nome;

    private String descricion;

    @NotNull
    private Double latitude;

    @NotNull
    private Double lonxitude;
}
