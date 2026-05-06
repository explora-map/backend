package explora.map.dto;

import explora.map.entity.EstadoConvite;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConviteResponseDTO {
    private UUID token;
    private Long mapaId;
    private String mapaNome;
    private String usernameAnfitrioa;
    private String usernameConvidada;
    private EstadoConvite estado;
    private LocalDateTime dataExpiracion;
}
