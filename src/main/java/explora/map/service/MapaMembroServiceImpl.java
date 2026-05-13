package explora.map.service;

import explora.map.dto.MapaMembroResponseDTO;
import explora.map.entity.MapaMembro;
import explora.map.entity.Mapa;
import explora.map.entity.RolMapa;
import explora.map.repository.MapaMembroRepository;
import explora.map.repository.MapaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapaMembroServiceImpl implements MapaMembroService {

    private final MapaMembroRepository mapaMembroRepository;
    private final MapaRepository mapaRepository;
    private final MapaAccesoService mapaAccesoService;

    @Transactional(readOnly = true)
    @Override
    public List<MapaMembroResponseDTO> listarMembros(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        mapaAccesoService.verificar(mapa, username);
        return mapaMembroRepository.findAllByMapaId(mapaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public MapaMembroResponseDTO cambiarRol(Long mapaId, String usernameObxectivo, RolMapa novoRol, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        if (!mapa.getCreadoPor().equals(username)) {
            throw new IllegalStateException("Sen permiso para cambiar roles neste mapa");
        }
        MapaMembro membro = mapaMembroRepository.findByMapaIdAndUsuariaUsername(mapaId, usernameObxectivo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Membro non atopado no mapa: " + usernameObxectivo));
        membro.setRol(novoRol);
        return toDTO(mapaMembroRepository.save(membro));
    }

    @Transactional
    @Override
    public void eliminarMembro(Long mapaId, String usernameObxectivo, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        if (!mapa.getCreadoPor().equals(username)) {
            throw new IllegalStateException("Sen permiso para eliminar membros deste mapa");
        }
        if (usernameObxectivo.equals(username)) {
            throw new IllegalArgumentException("O propietario non se pode eliminar a si mesmo como membro");
        }
        mapaMembroRepository.deleteByMapaIdAndUsuariaUsername(mapaId, usernameObxectivo);
    }

    @Override
    public void verificarPermisoEscritura(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        if (mapa.getCreadoPor().equals(username)) {
            return;
        }
        boolean tenPermiso = mapaMembroRepository.findByMapaIdAndUsuariaUsername(mapaId, username)
                .map(m -> m.getRol() == RolMapa.COLABORADORA || m.getRol() == RolMapa.ADMIN_MAPA)
                .orElse(false);
        if (!tenPermiso) {
            throw new IllegalStateException("Sen permiso de edición neste mapa");
        }
    }

    private MapaMembroResponseDTO toDTO(MapaMembro m) {
        MapaMembroResponseDTO dto = new MapaMembroResponseDTO();
        dto.setUsername(m.getUsuaria().getUsername());
        dto.setRol(m.getRol());
        return dto;
    }
}
