package explora.map.dto;

import explora.map.entity.EntradaHistorial;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntradaHistorialResponseDTO {

    private Long id;
    private String usuaria;
    private String tipoAccion;
    private String tipoElemento;
    private Long elementoId;
    private String elementoNome;
    private String detalle;
    private LocalDateTime dataAccion;

    public static EntradaHistorialResponseDTO fromEntity(EntradaHistorial e) {
        return EntradaHistorialResponseDTO.builder()
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
