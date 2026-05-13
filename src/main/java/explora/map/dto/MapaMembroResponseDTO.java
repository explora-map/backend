package explora.map.dto;

import explora.map.entity.RolMapa;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapaMembroResponseDTO {
    private String username;
    private RolMapa rol;
}
