package explora.map.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaResponseDTO {
    private Long id;
    private String nome;
    private String cor;
    private String icona;
    private Long mapaId;
    private String creadoPor;
    private LocalDateTime dataCreacion;
    private LocalDateTime dataModificacion;
}
