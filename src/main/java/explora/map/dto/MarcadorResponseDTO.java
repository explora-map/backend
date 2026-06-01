package explora.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** DTO de saída con os datos dun marcador. */
@Schema(description = "Datos públicos dun marcador xeográfico")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarcadorResponseDTO {
    @Schema(description = "Identificador único do marcador", example = "1")
    private Long id;
    @Schema(description = "Nome do marcador", example = "Praia de Samil")
    private String nome;
    @Schema(description = "Descrición do marcador", example = "Praia urbana con paseo marítimo")
    private String descricion;
    @Schema(description = "Latitude do marcador", example = "42.2328")
    private Double latitude;
    @Schema(description = "Lonxitude do marcador", example = "-8.7226")
    private Double lonxitude;
    @Schema(description = "Identificador do mapa ao que pertence o marcador", example = "1")
    private Long mapaId;
    @Schema(description = "Username da usuaria que creou o marcador", example = "maria_g")
    private String creadoPor;
    @Schema(description = "Data e hora de creación do marcador", example = "2026-04-15T10:30:00")
    private LocalDateTime dataCreacion;
    @Schema(description = "Data e hora da última modificación do marcador", example = "2026-04-15T10:30:00")
    private LocalDateTime dataModificacion;
    @Schema(description = "Identificador da categoría do marcador", example = "1")
    private Long categoriaId;
    @Schema(description = "Nome da categoría do marcador", example = "Praias")
    private String categoriaNome;
    @Schema(description = "Cor hexadecimal da categoría do marcador", example = "#FF5733")
    private String categoriaCor;
    @Schema(description = "Icona da categoría do marcador", example = "beach")
    private String categoriaIcona;
}
