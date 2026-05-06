package explora.map.repository;

import explora.map.entity.Mapa;
import explora.map.entity.TipoMapa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MapaRepository extends JpaRepository<Mapa, Long> {
    List<Mapa> findByCreadoPor(String username);
    List<Mapa> findByTipo(TipoMapa tipo);

    // TODO(Sprint 4): replace Java-side Haversine filtering with a DB-native query.
    // H2 lacks trig functions in JPQL; a PostgreSQL native query using
    // (6371 * acos(cos(radians(?1)) * cos(radians(m.latitude)) *
    //  cos(radians(m.lonxitude) - radians(?2)) + sin(radians(?1)) *
    //  sin(radians(m.latitude)))) <= ?3
    // would push filtering to the DB and avoid a full-table scan in prod.
}
