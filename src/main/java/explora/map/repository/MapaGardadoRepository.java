package explora.map.repository;

import explora.map.entity.MapaGardado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MapaGardadoRepository extends JpaRepository<MapaGardado, Long> {
    List<MapaGardado> findAllByUsuariaUsername(String username);
    Optional<MapaGardado> findByMapaIdAndUsuariaUsername(Long mapaId, String username);
    boolean existsByMapaIdAndUsuariaUsername(Long mapaId, String username);
    void deleteByMapaIdAndUsuariaUsername(Long mapaId, String username);
    void deleteAllByUsuariaUsername(String username);
}
