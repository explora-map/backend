package explora.map.repository;

import explora.map.entity.Marcador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarcadorRepository extends JpaRepository<Marcador, Long> {
    List<Marcador> findByMapaId(Long mapaId);
}
