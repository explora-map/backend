package explora.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "Datos públicos dunha categoría de marcadores")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaResponseDTO {
    @Schema(description = "Identificador único da categoría", example = "1")
    private Long id;
    @Schema(description = "Nome da categoría", example = "Praias")
    private String nome;
    @Schema(description = "Cor hexadecimal da categoría", example = "#FF5733")
    private String cor;
    @Schema(description = "Icona da categoría", example = "beach")
    private String icona;
    @Schema(description = "Identificador do mapa ao que pertence a categoría", example = "1")
    private Long mapaId;
    @Schema(description = "Username da usuaria que creou a categoría", example = "maria_g")
    private String creadoPor;
    @Schema(description = "Data e hora de creación da categoría", example = "2026-04-15T10:30:00")
    private LocalDateTime dataCreacion;
    @Schema(description = "Data e hora da última modificación da categoría", example = "2026-04-15T10:30:00")
    private LocalDateTime dataModificacion;
}
