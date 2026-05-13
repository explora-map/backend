package explora.map.repository;

import explora.map.entity.EntradaHistorial;
import explora.map.entity.TipoElemento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntradaHistorialRepository extends JpaRepository<EntradaHistorial, Long> {

    List<EntradaHistorial> findByMapaIdOrderByDataAccionDesc(Long mapaId);

    List<EntradaHistorial> findByMapaIdAndTipoElementoOrderByDataAccionDesc(Long mapaId, TipoElemento tipoElemento);

    List<EntradaHistorial> findByMapaIdAndUsuariaOrderByDataAccionDesc(Long mapaId, String usuaria);

    @Modifying
    @Query("DELETE FROM EntradaHistorial e WHERE e.mapa.id = :mapaId")
    void deleteByMapaId(@Param("mapaId") Long mapaId);
}
