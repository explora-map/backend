package explora.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "Datos do perfil da usuaria autenticada")
@Data
@Builder
public class PerfilResponseDTO {
    @Schema(description = "Identificador único da usuaria", example = "1")
    private Long id;
    @Schema(description = "Nome completo da usuaria", example = "María García")
    private String nome;
    @Schema(description = "Nome de usuario único", example = "maria_g")
    private String username;
    @Schema(description = "Enderezo de correo electrónico da usuaria", example = "maria@exemplo.gal")
    private String correo;
    @Schema(description = "Rol da usuaria na plataforma", example = "USUARIA")
    private String rol;
    @Schema(description = "Código de idioma preferido (gl ou en)", example = "gl")
    private String idioma;
    @Schema(description = "Data e hora de creación da conta", example = "2026-04-15T10:30:00")
    private String dataCreacion;
}
