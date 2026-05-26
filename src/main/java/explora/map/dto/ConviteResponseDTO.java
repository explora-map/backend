package explora.map.dto;

import explora.map.entity.EstadoConvite;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Datos públicos dun convite a un mapa privado")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConviteResponseDTO {
    @Schema(description = "Token único do convite (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID token;
    @Schema(description = "Identificador do mapa ao que invita o convite", example = "1")
    private Long mapaId;
    @Schema(description = "Nome do mapa ao que invita o convite", example = "Roteiro pola costa")
    private String mapaNome;
    @Schema(description = "Username da usuaria que enviou o convite", example = "maria_g")
    private String usernameAnfitrioa;
    @Schema(description = "Username da usuaria convidada", example = "ana_p")
    private String usernameConvidada;
    @Schema(description = "Estado actual do convite", example = "PENDENTE")
    private EstadoConvite estado;
    @Schema(description = "Data e hora de expiración do convite", example = "2026-04-15T10:30:00")
    private LocalDateTime dataExpiracion;
}
