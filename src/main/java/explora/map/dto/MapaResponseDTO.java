package explora.map.dto;

import explora.map.entity.TipoMapa;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapaResponseDTO {
    private Long id;
    private String nome;
    private String descricion;
    private Double latitude;
    private Double lonxitude;
    private String nomeLocalizacion;
    private TipoMapa tipo;
    private String creadoPor;
    private LocalDateTime dataCreacion;
    private String cidade;
    private String rexion;
    private String pais;
    private String codigoPais;
}
