package explora.map.repository;

import explora.map.entity.MapaMembro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MapaMembroRepository extends JpaRepository<MapaMembro, Long> {
    Optional<MapaMembro> findByMapaIdAndUsuariaUsername(Long mapaId, String username);
    List<MapaMembro> findAllByMapaId(Long mapaId);
    List<MapaMembro> findAllByUsuariaUsername(String username);
    boolean existsByMapaIdAndUsuariaUsername(Long mapaId, String username);
    void deleteByMapaIdAndUsuariaUsername(Long mapaId, String username);
    void deleteAllByUsuariaUsername(String username);
}
