package explora.map.repository;

import explora.map.entity.Historial;
import explora.map.entity.TipoElemento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistorialRepository extends JpaRepository<Historial, Long> {

    List<Historial> findByMapaIdOrderByDataAccionDesc(Long mapaId);

    List<Historial> findByMapaIdAndTipoElementoOrderByDataAccionDesc(Long mapaId, TipoElemento tipoElemento);

    List<Historial> findByMapaIdAndUsuariaOrderByDataAccionDesc(Long mapaId, String usuaria);

    @Modifying
    @Query("DELETE FROM Historial e WHERE e.mapa.id = :mapaId")
    void deleteByMapaId(@Param("mapaId") Long mapaId);
}
