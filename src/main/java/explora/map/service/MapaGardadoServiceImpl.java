package explora.map.service;

import explora.map.dto.MapaResponseDTO;
import explora.map.entity.Mapa;
import explora.map.entity.MapaGardado;
import explora.map.entity.TipoMapa;
import explora.map.entity.Usuaria;
import explora.map.repository.MapaGardadoRepository;
import explora.map.repository.MapaRepository;
import explora.map.repository.UsuariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapaGardadoServiceImpl implements MapaGardadoService {

    private final MapaGardadoRepository mapaGardadoRepository;
    private final MapaRepository mapaRepository;
    private final UsuariaRepository usuariaRepository;

    @Transactional
    @Override
    public void gardarMapa(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        if (mapa.getTipo() != TipoMapa.PUBLICO) {
            throw new IllegalStateException("Só se poden gardar mapas públicos");
        }
        if (mapa.getCreadoPor().equals(username)) {
            throw new IllegalStateException("Non podes gardar o teu propio mapa");
        }
        if (mapaGardadoRepository.existsByMapaIdAndUsuariaUsername(mapaId, username)) {
            throw new IllegalStateException("Mapa xa gardado");
        }
        Usuaria usuaria = usuariaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        MapaGardado gardado = MapaGardado.builder()
                .mapa(mapa)
                .usuaria(usuaria)
                .build();
        mapaGardadoRepository.save(gardado);
    }

    @Transactional
    @Override
    public void desgardarMapa(Long mapaId, String username) {
        if (!mapaGardadoRepository.existsByMapaIdAndUsuariaUsername(mapaId, username)) {
            throw new IllegalArgumentException("O mapa non está gardado: " + mapaId);
        }
        mapaGardadoRepository.deleteByMapaIdAndUsuariaUsername(mapaId, username);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MapaResponseDTO> obterMapasGardados(String username) {
        return mapaGardadoRepository.findAllByUsuariaUsername(username)
                .stream()
                .map(g -> toDTO(g.getMapa()))
                .collect(Collectors.toList());
    }

    private MapaResponseDTO toDTO(Mapa m) {
        return new MapaResponseDTO(
                m.getId(),
                m.getNome(),
                m.getDescricion(),
                m.getLatitude(),
                m.getLonxitude(),
                m.getNomeLocalizacion(),
                m.getTipo(),
                m.getCreadoPor(),
                m.getDataCreacion(),
                m.getCidade(),
                m.getRexion(),
                m.getPais(),
                m.getCodigoPais()
        );
    }
}
