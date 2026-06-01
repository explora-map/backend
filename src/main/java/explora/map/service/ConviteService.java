package explora.map.service;

import explora.map.dto.ConviteResponseDTO;
import explora.map.dto.ConviteRequestDTO;

import java.util.List;
import java.util.UUID;

/** Interface do servizo de xestión de convites entre usuarias. */
public interface ConviteService {
    ConviteResponseDTO novo(String usernameAnfitrioa, ConviteRequestDTO conviteNovoDTO);
    List<ConviteResponseDTO> obterPorUsername(String usernameAnfitrioa);
    List<ConviteResponseDTO> obterRecibidos(String usernameConvidada);
    void aceptar(UUID token, String username);
    void rexeitar(UUID token, String username);
    void cancelar(UUID token, String username);
}
