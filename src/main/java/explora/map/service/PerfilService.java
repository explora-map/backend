package explora.map.service;

import explora.map.dto.PerfilRequestDTO;
import explora.map.dto.PerfilResponseDTO;

public interface PerfilService {
    PerfilResponseDTO obterPerfil(String username);
    PerfilResponseDTO actualizarPerfil(String username, PerfilRequestDTO dto);
    void eliminar(String username);
}
