package explora.map.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO de entrada para actualizar o perfil da usuaria. Todos os campos son opcionais (patch parcial). */
@Data
public class PerfilRequestDTO {
    @Size(min = 3, max = 20)
    private String nome;

    @Size(min = 4, max = 20)
    private String username;

    @Email
    @Size(min = 8, max = 50)
    private String correo;

    private String contrasinelActual;
    private String contrasinelNovo;
    private String contrasinelNovoConfirmacion;

    private String idioma;
}
