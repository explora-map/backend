package explora.map.service;

import explora.map.dto.CategoriaRequestDTO;
import explora.map.dto.CategoriaResponseDTO;
import explora.map.entity.Categoria;
import explora.map.entity.Mapa;
import explora.map.entity.MapaMembro;
import explora.map.entity.RolMapa;
import explora.map.entity.TipoAccion;
import explora.map.entity.TipoElemento;
import explora.map.repository.CategoriaRepository;
import explora.map.repository.MapaMembroRepository;
import explora.map.repository.MapaRepository;
import explora.map.repository.MarcadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación do servizo de xestión de categorías de marcadores.
 *
 * <p>Proporciona operacións CRUD sobre as categorías dentro dun mapa colaborativo.
 * O control de acceso é o seguinte:</p>
 * <ul>
 *   <li>Calquera membro con acceso ao mapa pode consultar as categorías.</li>
 *   <li>Só COLABORADORA, ADMIN_MAPA e a propietaria poden crear categorías.</li>
 *   <li>COLABORADORA só pode editar ou eliminar as súas propias categorías.</li>
 *   <li>ADMIN_MAPA e a propietaria poden editar ou eliminar calquera categoría.</li>
 *   <li>Ao eliminar unha categoría, os marcadores asociados quedan sen categoría asignada.</li>
 * </ul>
 * <p>Cada operación de escritura queda rexistrada no historial do mapa.</p>
 */
@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final MapaRepository mapaRepository;
    private final MapaAccesoService mapaAccesoService;
    private final MarcadorRepository marcadorRepository;
    private final MapaMembroService mapaMembroService;
    private final MapaMembroRepository mapaMembroRepository;
    private final HistorialService historialService;

    /**
     * Devolve todas as categorías dun mapa.
     *
     * @param mapaId   identificador do mapa
     * @param username usuaria que solicita a listaxe
     * @return lista de categorías do mapa
     * @throws IllegalArgumentException se o mapa non existe
     * @throws IllegalStateException    se a usuaria non ten acceso ao mapa
     */
    @Transactional(readOnly = true)
    @Override
    public List<CategoriaResponseDTO> listarPorMapa(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        mapaAccesoService.verificar(mapa, username);
        return categoriaRepository.findByMapaId(mapaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Crea unha nova categoría nun mapa colaborativo.
     *
     * <p>Require rol COLABORADORA, ADMIN_MAPA ou ser propietaria do mapa.
     * Rexistra a acción no historial.</p>
     *
     * @param mapaId   identificador do mapa no que se crea a categoría
     * @param dto      datos da nova categoría: nome, cor e icona
     * @param username usuaria creadora
     * @return DTO coa categoría creada
     * @throws IllegalArgumentException se o mapa non existe
     * @throws IllegalStateException    se a usuaria non ten permiso de escritura no mapa
     */
    @Transactional
    @Override
    public CategoriaResponseDTO crear(Long mapaId, CategoriaRequestDTO dto, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        mapaMembroService.verificarPermisoEscritura(mapaId, username);
        Categoria categoria = new Categoria();
        categoria.setNome(dto.getNome());
        categoria.setCor(dto.getCor());
        categoria.setIcona(dto.getIcona());
        categoria.setMapa(mapa);
        categoria.setCreadoPor(username);
        Categoria gardada = categoriaRepository.save(categoria);
        historialService.rexistrar(
                gardada.getMapa(),
                username,
                TipoAccion.CREAR,
                TipoElemento.CATEGORIA,
                gardada.getId(),
                gardada.getNome(),
                null
        );
        return toDTO(gardada);
    }

    /**
     * Edita os datos dunha categoría existente.
     *
     * <p>COLABORADORA só pode editar as súas propias categorías.
     * ADMIN_MAPA e a propietaria poden editar calquera.
     * Rexistra a acción no historial.</p>
     *
     * @param id       identificador da categoría a editar
     * @param dto      novos datos da categoría: nome, cor e icona
     * @param username usuaria que solicita a edición
     * @return DTO coa categoría actualizada
     * @throws IllegalArgumentException se a categoría non existe
     * @throws IllegalStateException    se a usuaria non ten permiso para editar esta categoría
     */
    @Transactional
    @Override
    public CategoriaResponseDTO editar(Long id, CategoriaRequestDTO dto, String username) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría non atopada: " + id));
        verificarPermisosEdicion(categoria.getMapa(), categoria.getCreadoPor(), username);
        categoria.setNome(dto.getNome());
        categoria.setCor(dto.getCor());
        categoria.setIcona(dto.getIcona());
        Categoria editada = categoriaRepository.save(categoria);
        historialService.rexistrar(
                editada.getMapa(),
                username,
                TipoAccion.EDITAR,
                TipoElemento.CATEGORIA,
                editada.getId(),
                editada.getNome(),
                null
        );
        return toDTO(editada);
    }

    /**
     * Elimina unha categoría do mapa.
     *
     * <p>Os marcadores asociados á categoría eliminada quedan sen categoría asignada.
     * COLABORADORA só pode eliminar as súas propias categorías.
     * ADMIN_MAPA e a propietaria poden eliminar calquera.
     * Rexistra a acción no historial antes da eliminación.</p>
     *
     * @param id       identificador da categoría a eliminar
     * @param username usuaria que solicita a eliminación
     * @throws IllegalArgumentException se a categoría non existe
     * @throws IllegalStateException    se a usuaria non ten permiso para eliminar esta categoría
     */
    @Transactional
    @Override
    public void eliminar(Long id, String username) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría non atopada: " + id));
        verificarPermisosEdicion(categoria.getMapa(), categoria.getCreadoPor(), username);
        historialService.rexistrar(
                categoria.getMapa(),
                username,
                TipoAccion.ELIMINAR,
                TipoElemento.CATEGORIA,
                categoria.getId(),
                categoria.getNome(),
                null
        );
        marcadorRepository.desasociarCategoria(id);
        categoriaRepository.delete(categoria);
    }

    private void verificarPermisosEdicion(Mapa mapa, String creadoPorElemento, String username) {
        if (mapa.getCreadoPor().equals(username)) return;

        Optional<MapaMembro> membro = mapaMembroRepository
                .findByMapaIdAndUsuariaUsername(mapa.getId(), username);

        if (membro.isEmpty()) {
            throw new IllegalStateException("Sen permisos para modificar este elemento");
        }

        RolMapa rol = membro.get().getRol();

        if (rol == RolMapa.MEMBRO) {
            throw new IllegalStateException("Os membros non poden modificar elementos do mapa");
        }

        if (rol == RolMapa.COLABORADORA && !creadoPorElemento.equals(username)) {
            throw new IllegalStateException("Os colaboradores só poden modificar os seus propios elementos");
        }
    }

    private CategoriaResponseDTO toDTO(Categoria c) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(c.getId());
        dto.setNome(c.getNome());
        dto.setCor(c.getCor());
        dto.setIcona(c.getIcona());
        dto.setMapaId(c.getMapa().getId());
        dto.setCreadoPor(c.getCreadoPor());
        dto.setDataCreacion(c.getDataCreacion());
        dto.setDataModificacion(c.getDataModificacion());
        return dto;
    }
}
