package explora.map.dto;

import explora.map.entity.Historial;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialResponseDTO {

    private Long id;
    private String usuaria;
    private String tipoAccion;
    private String tipoElemento;
    private Long elementoId;
    private String elementoNome;
    private String detalle;
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
