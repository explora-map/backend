package explora.map.service;

import explora.map.dto.CategoriaRequestDTO;
import explora.map.dto.CategoriaResponseDTO;
import explora.map.entity.Categoria;
import explora.map.entity.Mapa;
import explora.map.repository.CategoriaRepository;
import explora.map.repository.MapaRepository;
import explora.map.repository.MarcadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final MapaRepository mapaRepository;
    private final MapaAccesoService mapaAccesoService;
    private final MarcadorRepository marcadorRepository;
    private final MapaMembroService mapaMembroService;

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
        return toDTO(categoriaRepository.save(categoria));
    }

    @Transactional
    @Override
    public CategoriaResponseDTO editar(Long id, CategoriaRequestDTO dto, String username) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría non atopada: " + id));
        mapaMembroService.verificarPermisoEscritura(categoria.getMapa().getId(), username);
        categoria.setNome(dto.getNome());
        categoria.setCor(dto.getCor());
        categoria.setIcona(dto.getIcona());
        return toDTO(categoriaRepository.save(categoria));
    }

    @Transactional
    @Override
    public void eliminar(Long id, String username) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría non atopada: " + id));
        mapaMembroService.verificarPermisoEscritura(categoria.getMapa().getId(), username);
        marcadorRepository.desasociarCategoria(id);
        categoriaRepository.delete(categoria);
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
