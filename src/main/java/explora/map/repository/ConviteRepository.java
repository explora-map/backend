package explora.map.repository;

import explora.map.entity.Convite;
import explora.map.entity.Usuaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConviteRepository extends JpaRepository<Convite, Long> {
    Optional<Convite> findByToken(UUID token);
    List<Convite> findByAnfitrioa(Usuaria anfitrioa);
    List<Convite> findByConvidada(Usuaria convidada);
    List<Convite> findByMapaId(Long mapaId);
}
