package explora.map.service;

import explora.map.dto.EntradaHistorialResponseDTO;
import explora.map.entity.EntradaHistorial;
import explora.map.entity.Mapa;
import explora.map.entity.TipoAccion;
import explora.map.entity.TipoElemento;
import explora.map.repository.EntradaHistorialRepository;
import explora.map.repository.MapaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialServiceImpl implements HistorialService {

    private final EntradaHistorialRepository historialRepository;
    private final MapaRepository mapaRepository;
    private final MapaAccesoService mapaAccesoService;

    @Override
    @Transactional
    public void rexistrar(Mapa mapa, String usuaria, TipoAccion accion, TipoElemento elemento, Long elementoId, String elementoNome, String detalle) {
        EntradaHistorial entrada = EntradaHistorial.builder()
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

    @Override
    @Transactional(readOnly = true)
    public List<EntradaHistorialResponseDTO> listarPorMapa(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado"));
        mapaAccesoService.verificar(mapa, username);
        return historialRepository.findByMapaIdOrderByDataAccionDesc(mapaId)
                .stream()
                .map(EntradaHistorialResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntradaHistorialResponseDTO> listarPorMapaETipo(Long mapaId, TipoElemento tipo, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado"));
        mapaAccesoService.verificar(mapa, username);
        return historialRepository.findByMapaIdAndTipoElementoOrderByDataAccionDesc(mapaId, tipo)
                .stream()
                .map(EntradaHistorialResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntradaHistorialResponseDTO> listarPorMapaEUsuaria(Long mapaId, String usuariaFiltro, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado"));
        mapaAccesoService.verificar(mapa, username);
        return historialRepository.findByMapaIdAndUsuariaOrderByDataAccionDesc(mapaId, usuariaFiltro)
                .stream()
                .map(EntradaHistorialResponseDTO::fromEntity)
                .toList();
    }
}
