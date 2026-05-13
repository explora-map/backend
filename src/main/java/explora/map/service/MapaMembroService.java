package explora.map.service;

import explora.map.dto.MapaMembroResponseDTO;
import explora.map.entity.RolMapa;

import java.util.List;

public interface MapaMembroService {
    List<MapaMembroResponseDTO> listarMembros(Long mapaId, String username);
    MapaMembroResponseDTO cambiarRol(Long mapaId, String usernameObxectivo, RolMapa novoRol, String username);
    void eliminarMembro(Long mapaId, String usernameObxectivo, String username);
    void verificarPermisoEscritura(Long mapaId, String username);
}
