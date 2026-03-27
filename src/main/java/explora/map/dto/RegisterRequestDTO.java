package explora.map.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {

    @NotBlank
    @Size (min = 3, max = 20)
    private String nome;

    @NotBlank
    @Size (min = 4, max = 20)
    private String username;

    @NotBlank
    @Size (min = 8, max = 50)
    @Email
    private String correo;

    @NotBlank
    @Size (min = 8, max = 40)
    private String password;
}
