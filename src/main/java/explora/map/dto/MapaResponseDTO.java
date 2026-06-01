package explora.map.dto;

import explora.map.entity.TipoMapa;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** DTO de saída con os datos dun mapa. */
@Schema(description = "Datos públicos dun mapa colaborativo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapaResponseDTO {
    @Schema(description = "Identificador único do mapa", example = "1")
    private Long id;
    @Schema(description = "Nome do mapa", example = "Roteiro pola costa")
    private String nome;
    @Schema(description = "Descrición do mapa", example = "Camiños e puntos de interese na costa galega")
    private String descricion;
    @Schema(description = "Latitude do centro do mapa", example = "42.2328")
    private Double latitude;
    @Schema(description = "Lonxitude do centro do mapa", example = "-8.7226")
    private Double lonxitude;
    @Schema(description = "Nome da localización seleccionada polo creador", example = "Vigo, Galicia")
    private String nomeLocalizacion;
    @Schema(description = "Visibilidade do mapa", example = "PUBLICO")
    private TipoMapa tipo;
    @Schema(description = "Username da usuaria creadora do mapa", example = "maria_g")
    private String creadoPor;
    @Schema(description = "Data e hora de creación do mapa", example = "2026-04-15T10:30:00")
    private LocalDateTime dataCreacion;
    @Schema(description = "Cidade obtida por xeolocalización inversa", example = "Vigo")
    private String cidade;
    @Schema(description = "Rexión obtida por xeolocalización inversa", example = "Galicia")
    private String rexion;
    @Schema(description = "País obtido por xeolocalización inversa", example = "España")
    private String pais;
    @Schema(description = "Código ISO 3166-1 alpha-2 do país", example = "ES")
    private String codigoPais;
}
