package explora.map.repository;

import explora.map.entity.TokenVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenVerificacionRepository extends JpaRepository<TokenVerificacion, Long> {
    Optional<TokenVerificacion> findByToken(String token);
    void deleteByUsuaria_Id(Long usuariaId);
}
