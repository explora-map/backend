package explora.map.service;

import explora.map.dto.CategoriaRequestDTO;
import explora.map.dto.CategoriaResponseDTO;

import java.util.List;

/** Interface do servizo de categorías dun mapa. */
public interface CategoriaService {
    List<CategoriaResponseDTO> listarPorMapa(Long mapaId, String username);
    CategoriaResponseDTO crear(Long mapaId, CategoriaRequestDTO dto, String username);
    CategoriaResponseDTO editar(Long id, CategoriaRequestDTO dto, String username);
    void eliminar(Long id, String username);
}
