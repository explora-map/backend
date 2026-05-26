package explora.map.dto;

import explora.map.entity.Historial;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "Entrada do historial de actividade dun mapa colaborativo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialResponseDTO {

    @Schema(description = "Identificador único da entrada do historial", example = "1")
    private Long id;
    @Schema(description = "Username da usuaria que realizou a acción", example = "maria_g")
    private String usuaria;
    @Schema(description = "Tipo de acción realizada", example = "CREAR")
    private String tipoAccion;
    @Schema(description = "Tipo de elemento afectado pola acción", example = "MARCADOR")
    private String tipoElemento;
    @Schema(description = "Identificador do elemento afectado no momento da acción", example = "1")
    private Long elementoId;
    @Schema(description = "Nome do elemento afectado no momento da acción", example = "Praia de Samil")
    private String elementoNome;
    @Schema(description = "Información adicional sobre o cambio realizado", example = "Visibilidade cambiada a PRIVADO")
    private String detalle;
    @Schema(description = "Data e hora en que se rexistrou a acción", example = "2026-04-15T10:30:00")
    private LocalDateTime dataAccion;

    public static HistorialResponseDTO fromEntity(Historial e) {
        return HistorialResponseDTO.builder()
                .id(e.getId())
                .usuaria(e.getUsuaria())
                .tipoAccion(e.getTipoAccion().name())
                .tipoElemento(e.getTipoElemento().name())
                .elementoId(e.getElementoId())
                .elementoNome(e.getElementoNome())
                .detalle(e.getDetalle())
                .dataAccion(e.getDataAccion())
                .build();
    }
}
