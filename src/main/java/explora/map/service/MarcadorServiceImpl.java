package explora.map.service;

import explora.map.dto.MarcadorRequestDTO;
import explora.map.dto.MarcadorResponseDTO;
import explora.map.entity.Categoria;
import explora.map.entity.Mapa;
import explora.map.entity.Marcador;
import explora.map.repository.CategoriaRepository;
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
    private final CategoriaRepository categoriaRepository;
    private final MapaAccesoService mapaAccesoService;
    private final MapaMembroService mapaMembroService;

    @Transactional(readOnly = true)
    @Override
    public List<MarcadorResponseDTO> listarPorMapa(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        mapaAccesoService.verificar(mapa, username);
        return marcadorRepository.findByMapaId(mapaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

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
        return toDTO(marcadorRepository.save(marcador));
    }

    @Transactional
    @Override
    public MarcadorResponseDTO editar(Long id, MarcadorRequestDTO dto, String username) {
        Marcador marcador = marcadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marcador non atopado: " + id));
        mapaMembroService.verificarPermisoEscritura(marcador.getMapa().getId(), username);
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
        return toDTO(marcadorRepository.save(marcador));
    }

    @Transactional
    @Override
    public void eliminar(Long id, String username) {
        Marcador marcador = marcadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marcador non atopado: " + id));
        mapaMembroService.verificarPermisoEscritura(marcador.getMapa().getId(), username);
        marcadorRepository.delete(marcador);
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
