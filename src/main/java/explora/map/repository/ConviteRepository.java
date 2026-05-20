package explora.map.repository;

import explora.map.entity.Convite;
import explora.map.entity.Usuaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConviteRepository extends JpaRepository<Convite, Long> {
    Optional<Convite> findByToken(UUID token);
    List<Convite> findByAnfitrioa(Usuaria anfitrioa);
    List<Convite> findByConvidada(Usuaria convidada);
    List<Convite> findByMapaId(Long mapaId);

    @Modifying
    @Query("DELETE FROM Convite c WHERE c.anfitrioa = :usuaria OR c.convidada = :usuaria")
    void deleteByAnfitriaoOrConvidada(@Param("usuaria") Usuaria usuaria);
}
