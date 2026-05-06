package explora.map.repository;

import explora.map.entity.Marcador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MarcadorRepository extends JpaRepository<Marcador, Long> {
    List<Marcador> findByMapaId(Long mapaId);

    @Modifying
    @Transactional
    @Query("UPDATE Marcador m SET m.categoria = NULL WHERE m.categoria.id = :categoriaId")
    void desasociarCategoria(@Param("categoriaId") Long categoriaId);
}
