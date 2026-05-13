package explora.map.service;

import explora.map.dto.EntradaHistorialResponseDTO;
import explora.map.entity.Mapa;
import explora.map.entity.TipoAccion;
import explora.map.entity.TipoElemento;

import java.util.List;

public interface HistorialService {

    void rexistrar(Mapa mapa, String usuaria, TipoAccion accion, TipoElemento elemento, Long elementoId, String elementoNome, String detalle);

    List<EntradaHistorialResponseDTO> listarPorMapa(Long mapaId, String username);

    List<EntradaHistorialResponseDTO> listarPorMapaETipo(Long mapaId, TipoElemento tipo, String username);

    List<EntradaHistorialResponseDTO> listarPorMapaEUsuaria(Long mapaId, String usuariaFiltro, String username);
}
