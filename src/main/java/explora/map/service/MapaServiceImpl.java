package explora.map.service;

import explora.map.dto.MapaRequestDTO;
import explora.map.dto.MapaResponseDTO;
import explora.map.entity.EstadoConvite;
import explora.map.entity.Mapa;
import explora.map.entity.TipoMapa;
import explora.map.repository.ConviteRepository;
import explora.map.repository.MapaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapaServiceImpl implements MapaService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final MapaRepository mapaRepository;
    private final ConviteRepository conviteRepository;

    @Transactional
    @Override
    public MapaResponseDTO novo(String username, MapaRequestDTO dto) {
        Mapa mapa = new Mapa();
        mapa.setNome(dto.getNome());
        mapa.setDescricion(dto.getDescricion());
        mapa.setLatitude(dto.getLatitude());
        mapa.setLonxitude(dto.getLonxitude());
        mapa.setNomeLocalizacion(dto.getNomeLocalizacion());
        mapa.setCidade(dto.getCidade());
        mapa.setRexion(dto.getRexion());
        mapa.setPais(dto.getPais());
        mapa.setCodigoPais(dto.getCodigoPais());
        mapa.setTipo(dto.getTipo());
        // Set creadoPor explicitly as a safety net for test contexts where
        // the SecurityContext may be empty and AuditorAware returns empty.
        mapa.setCreadoPor(username);
        return toDTO(mapaRepository.save(mapa));
    }

    @Transactional(readOnly = true)
    @Override
    public MapaResponseDTO obterPorId(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        if (mapa.getTipo() == TipoMapa.PUBLICO) {
            return toDTO(mapa);
        }
        if (mapa.getCreadoPor().equals(username)) {
            return toDTO(mapa);
        }
        boolean tenConviteAceptado = conviteRepository.findByMapaId(mapaId).stream()
                .anyMatch(c -> c.getEstado() == EstadoConvite.ACEPTADO
                        && c.getConvidada().getUsername().equals(username));
        if (!tenConviteAceptado) {
            throw new IllegalStateException("Sen permiso para ver este mapa");
        }
        return toDTO(mapa);
    }

    @Override
    public List<MapaResponseDTO> obterPorUsername(String username) {
        return mapaRepository.findByCreadoPor(username)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public MapaResponseDTO editar(Long id, String username, MapaRequestDTO dto) {
        Mapa mapa = mapaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + id));
        if (!mapa.getCreadoPor().equals(username)) {
            throw new IllegalStateException("Sen permiso para editar este mapa");
        }
        mapa.setNome(dto.getNome());
        mapa.setDescricion(dto.getDescricion());
        mapa.setLatitude(dto.getLatitude());
        mapa.setLonxitude(dto.getLonxitude());
        mapa.setNomeLocalizacion(dto.getNomeLocalizacion());
        mapa.setCidade(dto.getCidade());
        mapa.setRexion(dto.getRexion());
        mapa.setPais(dto.getPais());
        mapa.setCodigoPais(dto.getCodigoPais());
        mapa.setTipo(dto.getTipo());
        return toDTO(mapaRepository.save(mapa));
    }

    @Transactional
    @Override
    public void eliminar(Long id, String username) {
        Mapa mapa = mapaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + id));
        if (!mapa.getCreadoPor().equals(username)) {
            throw new IllegalStateException("Sen permiso para eliminar este mapa");
        }
        mapaRepository.delete(mapa);
    }

    @Override
    public List<MapaResponseDTO> obterPorTipoPublico(Double latitude, Double lonxitude, Double radio) {
        return mapaRepository.findByTipo(TipoMapa.PUBLICO)
                .stream()
                .filter(m -> haversine(latitude, lonxitude, m.getLatitude(), m.getLonxitude()) <= radio)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Transactional
    @Override
    public MapaResponseDTO cambiarVisibilidade(Long id, String username, TipoMapa tipo) {
        Mapa mapa = mapaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + id));
        if (!mapa.getCreadoPor().equals(username)) {
            throw new IllegalStateException("Sen permiso para cambiar a visibilidade deste mapa");
        }
        mapa.setTipo(tipo);
        return toDTO(mapaRepository.save(mapa));
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