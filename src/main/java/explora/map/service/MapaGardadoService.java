package explora.map.service;

import explora.map.dto.MapaResponseDTO;

import java.util.List;

public interface MapaGardadoService {
    void gardarMapa(Long mapaId, String username);
    void desgardarMapa(Long mapaId, String username);
    List<MapaResponseDTO> obterMapasGardados(String username);
}
