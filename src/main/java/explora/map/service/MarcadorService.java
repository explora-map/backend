package explora.map.service;

import explora.map.dto.MarcadorRequestDTO;
import explora.map.dto.MarcadorResponseDTO;

import java.util.List;

/** Interface do servizo de marcadores xeográficos. */
public interface MarcadorService {
    List<MarcadorResponseDTO> listarPorMapa(Long mapaId, String username);
    MarcadorResponseDTO crear(Long mapaId, MarcadorRequestDTO dto, String username);
    MarcadorResponseDTO editar(Long id, MarcadorRequestDTO dto, String username);
    void eliminar(Long id, String username);
}
