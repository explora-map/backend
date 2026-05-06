package explora.map.dto;

import explora.map.entity.TipoMapa;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapaRequestDTO {

    @NotBlank
    private String nome;

    private String descricion;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double lonxitude;

    @NotBlank
    private String nomeLocalizacion;

    @NotNull
    private TipoMapa tipo;

    private String cidade;

    private String rexion;

    private String pais;

    @Size(max = 2)
    private String codigoPais;
}
