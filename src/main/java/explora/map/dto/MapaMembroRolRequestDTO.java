package explora.map.dto;

import explora.map.entity.RolMapa;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO de entrada para cambiar o rol dun membro nun mapa. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapaMembroRolRequestDTO {

    @NotNull
    private RolMapa rol;
}
