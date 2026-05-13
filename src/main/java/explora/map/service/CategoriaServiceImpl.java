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

    @Transactional(readOnly = true)
    @Override
    public List<CategoriaResponseDTO> listarPorMapa(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        mapaAccesoService.verificar(mapa, username);
        return categoriaRepository.findByMapaId(mapaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

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
