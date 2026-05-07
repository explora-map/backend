package explora.map.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PerfilRequestDTO {
    @Size(min = 3, max = 20)
    private String nome;

    @Size(min = 4, max = 20)
    private String username;

    @Email
    @Size(min = 8, max = 50)
    private String correo;

    @Size(min = 8, max = 40)
    private String password;

    private String idioma;
}
