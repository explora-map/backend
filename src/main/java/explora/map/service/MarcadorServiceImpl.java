package explora.map.service;

import explora.map.dto.MarcadorRequestDTO;
import explora.map.dto.MarcadorResponseDTO;
import explora.map.entity.EstadoConvite;
import explora.map.entity.Mapa;
import explora.map.entity.Marcador;
import explora.map.entity.TipoMapa;
import explora.map.repository.ConviteRepository;
import explora.map.repository.MarcadorRepository;
import explora.map.repository.MapaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarcadorServiceImpl implements MarcadorService {

    private final MarcadorRepository marcadorRepository;
    private final MapaRepository mapaRepository;
    private final ConviteRepository conviteRepository;

    @Transactional(readOnly = true)
    @Override
    public List<MarcadorResponseDTO> listarPorMapa(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        verificarAccesoMapa(mapa, username);
        return marcadorRepository.findByMapaId(mapaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public MarcadorResponseDTO crear(Long mapaId, MarcadorRequestDTO dto, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        verificarAccesoMapa(mapa, username);
        Marcador marcador = new Marcador();
        marcador.setNome(dto.getNome());
        marcador.setDescricion(dto.getDescricion());
        marcador.setLatitude(dto.getLatitude());
        marcador.setLonxitude(dto.getLonxitude());
        marcador.setMapa(mapa);
        marcador.setCreadoPor(username);
        return toDTO(marcadorRepository.save(marcador));
    }

    @Transactional
    @Override
    public MarcadorResponseDTO editar(Long id, MarcadorRequestDTO dto, String username) {
        Marcador marcador = marcadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marcador non atopado: " + id));
        if (!marcador.getCreadoPor().equals(username)) {
            throw new IllegalStateException("Sen permiso para editar este marcador");
        }
        marcador.setNome(dto.getNome());
        marcador.setDescricion(dto.getDescricion());
        marcador.setLatitude(dto.getLatitude());
        marcador.setLonxitude(dto.getLonxitude());
        return toDTO(marcadorRepository.save(marcador));
    }

    @Transactional
    @Override
    public void eliminar(Long id, String username) {
        Marcador marcador = marcadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marcador non atopado: " + id));
        if (!marcador.getCreadoPor().equals(username)) {
            throw new IllegalStateException("Sen permiso para eliminar este marcador");
        }
        marcadorRepository.delete(marcador);
    }

    private void verificarAccesoMapa(Mapa mapa, String username) {
        if (mapa.getTipo() == TipoMapa.PUBLICO) {
            return;
        }
        if (mapa.getCreadoPor().equals(username)) {
            return;
        }
        boolean tenConviteAceptado = conviteRepository.findByMapaId(mapa.getId()).stream()
                .anyMatch(c -> c.getEstado() == EstadoConvite.ACEPTADO
                        && c.getConvidada().getUsername().equals(username));
        if (!tenConviteAceptado) {
            throw new IllegalStateException("Sen permiso para acceder a este mapa");
        }
    }

    private MarcadorResponseDTO toDTO(Marcador m) {
        return new MarcadorResponseDTO(
                m.getId(),
                m.getNome(),
                m.getDescricion(),
                m.getLatitude(),
                m.getLonxitude(),
                m.getMapa().getId(),
                m.getCreadoPor(),
                m.getDataCreacion(),
                m.getDataModificacion()
        );
    }
}
