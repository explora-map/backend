package explora.map.service;

import explora.map.dto.MapaResponseDTO;
import explora.map.dto.MapaRequestDTO;
import explora.map.entity.TipoMapa;

import java.util.List;

public interface MapaService {
    MapaResponseDTO novo(String username, MapaRequestDTO mapaNovoDTO);
    MapaResponseDTO obterPorId(Long mapaId, String username);
    List<MapaResponseDTO> obterPorUsername(String username);
    MapaResponseDTO editar(Long id, String username, MapaRequestDTO mapaNovoDTO);
    void eliminar(Long id, String username);
    List<MapaResponseDTO> obterPorTipoPublico(Double latitude, Double lonxitude, Double radio);
    MapaResponseDTO cambiarVisibilidade(Long id, String username, TipoMapa tipo);
}
