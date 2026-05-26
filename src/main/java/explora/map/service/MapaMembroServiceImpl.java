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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación do servizo de xestión de membros en mapas colaborativos.
 *
 * <p>Permite consultar, modificar e eliminar membros dentro dun mapa.
 * O control de acceso é o seguinte:</p>
 * <ul>
 *   <li>Calquera membro con acceso ao mapa pode ver a lista de membros.</li>
 *   <li>Só ADMIN_MAPA e a propietaria poden cambiar roles e eliminar membros.</li>
 *   <li>A propietaria non pode ser eliminada como membro nin se lle pode cambiar o rol.</li>
 *   <li>Ao eliminar un membro revóganse os seus accesos ao mapa.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class MapaMembroServiceImpl implements MapaMembroService {

    private final MapaMembroRepository mapaMembroRepository;
    private final MapaRepository mapaRepository;
    private final MapaAccesoService mapaAccesoService;

    /**
     * Devolve todos os membros dun mapa co seu rol asignado.
     *
     * @param mapaId   identificador do mapa
     * @param username usuaria que solicita a listaxe
     * @return lista de membros do mapa co seu rol
     * @throws IllegalArgumentException se o mapa non existe
     * @throws IllegalStateException    se a usuaria non ten acceso ao mapa
     */
    @Transactional(readOnly = true)
    @Override
    public List<MapaMembroResponseDTO> listarMembros(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        mapaAccesoService.verificar(mapa, username);
        return mapaMembroRepository.findAllByMapaId(mapaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Cambia o rol dun membro dentro dun mapa colaborativo.
     *
     * <p>Só ADMIN_MAPA e a propietaria poden cambiar o rol dun membro.</p>
     *
     * @param mapaId            identificador do mapa
     * @param usernameObxectivo username do membro ao que se lle cambia o rol
     * @param novoRol           novo rol que se asignará ao membro
     * @param username          usuaria que solicita o cambio (debe ser ADMIN_MAPA ou propietaria)
     * @return DTO co membro actualizado e o novo rol
     * @throws IllegalArgumentException se o mapa ou o membro non existen no mapa indicado
     * @throws IllegalStateException    se a usuaria non ten permiso para xestionar membros
     */
    @Transactional
    @Override
    public MapaMembroResponseDTO cambiarRol(Long mapaId, String usernameObxectivo, RolMapa novoRol, String username) {
        verificarPermisoXestion(mapaId, username);
        MapaMembro membro = mapaMembroRepository.findByMapaIdAndUsuariaUsername(mapaId, usernameObxectivo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Membro non atopado no mapa: " + usernameObxectivo));
        membro.setRol(novoRol);
        return toDTO(mapaMembroRepository.save(membro));
    }

    /**
     * Elimina un membro do mapa, revogando o seu acceso.
     *
     * <p>Só ADMIN_MAPA e a propietaria poden eliminar membros.
     * A propietaria non pode ser eliminada como membro.</p>
     *
     * @param mapaId            identificador do mapa
     * @param usernameObxectivo username do membro a eliminar
     * @param username          usuaria que solicita a eliminación (debe ser ADMIN_MAPA ou propietaria)
     * @throws IllegalArgumentException se o mapa non existe ou se se intenta eliminar á propietaria
     * @throws IllegalStateException    se a usuaria non ten permiso para xestionar membros
     */
    @Transactional
    @Override
    public void eliminarMembro(Long mapaId, String usernameObxectivo, String username) {
        verificarPermisoXestion(mapaId, username);
        if (usernameObxectivo.equals(username)) {
            throw new IllegalArgumentException("O propietario non se pode eliminar a si mesmo como membro");
        }
        mapaMembroRepository.deleteByMapaIdAndUsuariaUsername(mapaId, usernameObxectivo);
    }

    /**
     * Verifica que a usuaria teña permiso de escritura no mapa indicado.
     *
     * <p>Teñen permiso de escritura: a propietaria do mapa, COLABORADORA e ADMIN_MAPA.
     * Os membros con rol {@code MEMBRO} non teñen permiso de escritura.</p>
     *
     * @param mapaId   identificador do mapa
     * @param username usuaria para a que se verifica o permiso
     * @throws IllegalArgumentException se o mapa non existe
     * @throws IllegalStateException    se a usuaria non ten permiso de escritura no mapa
     */
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

    private void verificarPermisoXestion(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado"));

        if (mapa.getCreadoPor().equals(username)) return;

        Optional<MapaMembro> membro = mapaMembroRepository
                .findByMapaIdAndUsuariaUsername(mapaId, username);

        if (membro.isEmpty() || membro.get().getRol() != RolMapa.ADMIN_MAPA) {
            throw new IllegalStateException("Sen permisos para xestionar membros deste mapa");
        }
    }

    private MapaMembroResponseDTO toDTO(MapaMembro m) {
        MapaMembroResponseDTO dto = new MapaMembroResponseDTO();
        dto.setUsername(m.getUsuaria().getUsername());
        dto.setRol(m.getRol());
        return dto;
    }
}
