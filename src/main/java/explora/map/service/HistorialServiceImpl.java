package explora.map.service;

import explora.map.dto.HistorialResponseDTO;
import explora.map.entity.Historial;
import explora.map.entity.Mapa;
import explora.map.entity.TipoAccion;
import explora.map.entity.TipoElemento;
import explora.map.repository.HistorialRepository;
import explora.map.repository.MapaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link HistorialService} que xestiona o rexistro e consulta
 * do historial de actividade dos mapas.
 *
 * <p>Cada acción relevante sobre un mapa, marcador ou categoría queda rexistrada
 * cunha entrada en {@link explora.map.entity.Historial}. As consultas verifican
 * previamente que a usuaria ten acceso de lectura ao mapa mediante
 * {@link MapaAccesoService}.</p>
 */
@Service
@RequiredArgsConstructor
public class HistorialServiceImpl implements HistorialService {

    private final HistorialRepository historialRepository;
    private final MapaRepository mapaRepository;
    private final MapaAccesoService mapaAccesoService;

    /**
     * Rexistra unha acción no historial de actividade do mapa.
     *
     * <p>Chámase internamente desde os servizos de mapas, marcadores e categorías
     * trala creación, edición ou eliminación dun elemento.</p>
     *
     * @param mapa        entidade do mapa ao que pertence a entrada de historial
     * @param usuaria     nome de usuaria que realizou a acción
     * @param accion      tipo de operación realizada
     *                    ({@link TipoAccion#CREAR}, {@link TipoAccion#EDITAR},
     *                    {@link TipoAccion#ELIMINAR})
     * @param elemento    tipo de elemento afectado
     *                    ({@link TipoElemento#MAPA}, {@link TipoElemento#MARCADOR},
     *                    {@link TipoElemento#CATEGORIA})
     * @param elementoId  identificador do elemento afectado no momento da acción
     * @param elementoNome nome do elemento afectado no momento da acción
     * @param detalle     información adicional opcional sobre o cambio; pode ser {@code null}
     */
    @Override
    @Transactional
    public void rexistrar(Mapa mapa, String usuaria, TipoAccion accion, TipoElemento elemento, Long elementoId, String elementoNome, String detalle) {
        Historial entrada = Historial.builder()
                .mapa(mapa)
                .usuaria(usuaria)
                .tipoAccion(accion)
                .tipoElemento(elemento)
                .elementoId(elementoId)
                .elementoNome(elementoNome)
                .detalle(detalle)
                .build();
        historialRepository.save(entrada);
    }

    /**
     * Obtén todo o historial de actividade dun mapa, ordenado do máis recente ao máis antigo.
     *
     * @param mapaId   identificador do mapa cuio historial se consulta
     * @param username nome da usuaria que solicita o historial
     * @return lista de DTOs con todas as entradas do historial do mapa;
     *         lista baleira se non hai actividade rexistrada
     * @throws IllegalArgumentException se non existe ningún mapa co id indicado
     * @throws IllegalStateException    se a usuaria non ten permiso de acceso ao mapa
     */
    @Override
    @Transactional(readOnly = true)
    public List<HistorialResponseDTO> listarPorMapa(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado"));
        mapaAccesoService.verificar(mapa, username);
        return historialRepository.findByMapaIdOrderByDataAccionDesc(mapaId)
                .stream()
                .map(HistorialResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Obtén o historial de actividade dun mapa filtrado por tipo de elemento,
     * ordenado do máis recente ao máis antigo.
     *
     * @param mapaId   identificador do mapa cuio historial se consulta
     * @param tipo     tipo de elemento polo que se filtra
     *                 ({@link TipoElemento#MAPA}, {@link TipoElemento#MARCADOR},
     *                 {@link TipoElemento#CATEGORIA})
     * @param username nome da usuaria que solicita o historial
     * @return lista de DTOs coas entradas do historial do tipo indicado;
     *         lista baleira se non hai actividade dese tipo
     * @throws IllegalArgumentException se non existe ningún mapa co id indicado
     * @throws IllegalStateException    se a usuaria non ten permiso de acceso ao mapa
     */
    @Override
    @Transactional(readOnly = true)
    public List<HistorialResponseDTO> listarPorMapaETipo(Long mapaId, TipoElemento tipo, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado"));
        mapaAccesoService.verificar(mapa, username);
        return historialRepository.findByMapaIdAndTipoElementoOrderByDataAccionDesc(mapaId, tipo)
                .stream()
                .map(HistorialResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Obtén o historial de actividade dun mapa filtrado polo nome da usuaria que realizou
     * as accións, ordenado do máis recente ao máis antigo.
     *
     * @param mapaId        identificador do mapa cuio historial se consulta
     * @param usuariaFiltro nome da usuaria cuias accións se queren ver
     * @param username      nome da usuaria autenticada que solicita o historial
     * @return lista de DTOs coas entradas do historial realizadas pola usuaria indicada;
     *         lista baleira se esa usuaria non ten accións rexistradas no mapa
     * @throws IllegalArgumentException se non existe ningún mapa co id indicado
     * @throws IllegalStateException    se a usuaria autenticada non ten permiso de acceso ao mapa
     */
    @Override
    @Transactional(readOnly = true)
    public List<HistorialResponseDTO> listarPorMapaEUsuaria(Long mapaId, String usuariaFiltro, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado"));
        mapaAccesoService.verificar(mapa, username);
        return historialRepository.findByMapaIdAndUsuariaOrderByDataAccionDesc(mapaId, usuariaFiltro)
                .stream()
                .map(HistorialResponseDTO::fromEntity)
                .toList();
    }
}
