package explora.map.service;

import explora.map.dto.HistorialResponseDTO;
import explora.map.entity.Mapa;
import explora.map.entity.TipoAccion;
import explora.map.entity.TipoElemento;

import java.util.List;

/** Interface do servizo de historial de cambios dun mapa. */
public interface HistorialService {

    void rexistrar(Mapa mapa, String usuaria, TipoAccion accion, TipoElemento elemento, Long elementoId, String elementoNome, String detalle);

    List<HistorialResponseDTO> listarPorMapa(Long mapaId, String username);

    List<HistorialResponseDTO> listarPorMapaETipo(Long mapaId, TipoElemento tipo, String username);

    List<HistorialResponseDTO> listarPorMapaEUsuaria(Long mapaId, String usuariaFiltro, String username);
}
