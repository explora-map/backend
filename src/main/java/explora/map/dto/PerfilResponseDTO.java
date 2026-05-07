package explora.map.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerfilResponseDTO {
    private Long id;
    private String nome;
    private String username;
    private String correo;
    private String rol;
    private String idioma;
    private String dataCreacion;
}
