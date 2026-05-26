package explora.map.service;

import explora.map.dto.MarcadorRequestDTO;
import explora.map.dto.MarcadorResponseDTO;
import explora.map.entity.Categoria;
import explora.map.entity.Mapa;
import explora.map.entity.MapaMembro;
import explora.map.entity.Marcador;
import explora.map.entity.RolMapa;
import explora.map.entity.TipoAccion;
import explora.map.entity.TipoElemento;
import explora.map.repository.CategoriaRepository;
import explora.map.repository.MarcadorRepository;
import explora.map.repository.MapaMembroRepository;
import explora.map.repository.MapaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación do servizo de xestión de marcadores xeográficos.
 *
 * <p>Proporciona operacións CRUD sobre os marcadores dentro dun mapa colaborativo.
 * O control de acceso é o seguinte:</p>
 * <ul>
 *   <li>Calquera membro con acceso ao mapa pode consultar os marcadores.</li>
 *   <li>Só COLABORADORA, ADMIN_MAPA e a propietaria poden crear marcadores.</li>
 *   <li>COLABORADORA só pode editar ou eliminar os seus propios marcadores.</li>
 *   <li>ADMIN_MAPA e a propietaria poden editar ou eliminar calquera marcador.</li>
 * </ul>
 * <p>Cada operación de escritura queda rexistrada no historial do mapa.</p>
 */
@Service
@RequiredArgsConstructor
public class MarcadorServiceImpl implements MarcadorService {

    private final MarcadorRepository marcadorRepository;
    private final MapaRepository mapaRepository;
    private final CategoriaRepository categoriaRepository;
    private final MapaAccesoService mapaAccesoService;
    private final MapaMembroService mapaMembroService;
    private final MapaMembroRepository mapaMembroRepository;
    private final HistorialService historialService;

    /**
     * Devolve todos os marcadores dun mapa.
     *
     * @param mapaId   identificador do mapa
     * @param username usuaria que solicita a listaxe
     * @return lista de marcadores do mapa
     * @throws IllegalArgumentException se o mapa non existe
     * @throws IllegalStateException    se a usuaria non ten acceso ao mapa
     */
    @Transactional(readOnly = true)
    @Override
    public List<MarcadorResponseDTO> listarPorMapa(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        mapaAccesoService.verificar(mapa, username);
        return marcadorRepository.findByMapaId(mapaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Crea un novo marcador xeográfico nun mapa colaborativo.
     *
     * <p>Require rol COLABORADORA, ADMIN_MAPA ou ser propietaria do mapa.
     * Se se indica categoría, debe pertencer ao mesmo mapa.
     * Rexistra a acción no historial.</p>
     *
     * @param mapaId   identificador do mapa no que se crea o marcador
     * @param dto      datos do novo marcador: nome, descrición, coordenadas e categoría opcional
     * @param username usuaria creadora
     * @return DTO co marcador creado
     * @throws IllegalArgumentException se o mapa ou a categoría indicada non existen
     * @throws IllegalStateException    se a usuaria non ten permiso de escritura no mapa
     */
    @Transactional
    @Override
    public MarcadorResponseDTO crear(Long mapaId, MarcadorRequestDTO dto, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        mapaMembroService.verificarPermisoEscritura(mapaId, username);
        Marcador marcador = new Marcador();
        marcador.setNome(dto.getNome());
        marcador.setDescricion(dto.getDescricion());
        marcador.setLatitude(dto.getLatitude());
        marcador.setLonxitude(dto.getLonxitude());
        marcador.setMapa(mapa);
        marcador.setCreadoPor(username);
        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría non atopada: " + dto.getCategoriaId()));
            marcador.setCategoria(categoria);
        }
        Marcador gardado = marcadorRepository.save(marcador);
        historialService.rexistrar(
                gardado.getMapa(),
                username,
                TipoAccion.CREAR,
                TipoElemento.MARCADOR,
                gardado.getId(),
                gardado.getNome(),
                null
        );
        return toDTO(gardado);
    }

    /**
     * Edita os datos dun marcador existente.
     *
     * <p>COLABORADORA só pode editar os seus propios marcadores.
     * ADMIN_MAPA e a propietaria poden editar calquera.
     * Rexistra a acción no historial.</p>
     *
     * @param id       identificador do marcador a editar
     * @param dto      novos datos do marcador: nome, descrición, coordenadas e categoría opcional
     * @param username usuaria que solicita a edición
     * @return DTO co marcador actualizado
     * @throws IllegalArgumentException se o marcador ou a categoría indicada non existen
     * @throws IllegalStateException    se a usuaria non ten permiso para editar este marcador
     */
    @Transactional
    @Override
    public MarcadorResponseDTO editar(Long id, MarcadorRequestDTO dto, String username) {
        Marcador marcador = marcadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marcador non atopado: " + id));
        verificarPermisosEdicion(marcador.getMapa(), marcador.getCreadoPor(), username);
        marcador.setNome(dto.getNome());
        marcador.setDescricion(dto.getDescricion());
        marcador.setLatitude(dto.getLatitude());
        marcador.setLonxitude(dto.getLonxitude());
        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría non atopada: " + dto.getCategoriaId()));
            marcador.setCategoria(categoria);
        } else {
            marcador.setCategoria(null);
        }
        Marcador editado = marcadorRepository.save(marcador);
        historialService.rexistrar(
                editado.getMapa(),
                username,
                TipoAccion.EDITAR,
                TipoElemento.MARCADOR,
                editado.getId(),
                editado.getNome(),
                null
        );
        return toDTO(editado);
    }

    /**
     * Elimina un marcador do mapa.
     *
     * <p>COLABORADORA só pode eliminar os seus propios marcadores.
     * ADMIN_MAPA e a propietaria poden eliminar calquera.
     * Rexistra a acción no historial antes da eliminación.</p>
     *
     * @param id       identificador do marcador a eliminar
     * @param username usuaria que solicita a eliminación
     * @throws IllegalArgumentException se o marcador non existe
     * @throws IllegalStateException    se a usuaria non ten permiso para eliminar este marcador
     */
    @Transactional
    @Override
    public void eliminar(Long id, String username) {
        Marcador marcador = marcadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marcador non atopado: " + id));
        verificarPermisosEdicion(marcador.getMapa(), marcador.getCreadoPor(), username);
        historialService.rexistrar(
                marcador.getMapa(),
                username,
                TipoAccion.ELIMINAR,
                TipoElemento.MARCADOR,
                marcador.getId(),
                marcador.getNome(),
                null
        );
        marcadorRepository.delete(marcador);
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
        // ADMIN_MAPA pode modificar calquera elemento
    }

    private MarcadorResponseDTO toDTO(Marcador m) {
        MarcadorResponseDTO dto = new MarcadorResponseDTO();
        dto.setId(m.getId());
        dto.setNome(m.getNome());
        dto.setDescricion(m.getDescricion());
        dto.setLatitude(m.getLatitude());
        dto.setLonxitude(m.getLonxitude());
        dto.setMapaId(m.getMapa().getId());
        dto.setCreadoPor(m.getCreadoPor());
        dto.setDataCreacion(m.getDataCreacion());
        dto.setDataModificacion(m.getDataModificacion());
        if (m.getCategoria() != null) {
            dto.setCategoriaId(m.getCategoria().getId());
            dto.setCategoriaNome(m.getCategoria().getNome());
            dto.setCategoriaCor(m.getCategoria().getCor());
            dto.setCategoriaIcona(m.getCategoria().getIcona());
        }
        return dto;
    }
}
