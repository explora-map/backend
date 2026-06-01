package explora.map.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO de entrada para crear ou editar unha categoría. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaRequestDTO {

    @NotBlank
    private String nome;

    @NotBlank
    private String cor;

    private String icona;
}
