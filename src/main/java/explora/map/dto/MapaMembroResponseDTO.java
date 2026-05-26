package explora.map.dto;

import explora.map.entity.RolMapa;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Datos dun membro dentro dun mapa colaborativo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapaMembroResponseDTO {
    @Schema(description = "Username da usuaria membro", example = "ana_p")
    private String username;
    @Schema(description = "Rol da usuaria dentro do mapa", example = "COLABORADOR")
    private RolMapa rol;
}
