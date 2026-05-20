package explora.map.repository;

import explora.map.entity.RefreshToken;
import explora.map.entity.Usuaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findByUsuariaAndIsRevokedFalse(Usuaria usuaria);
    void deleteByUsuaria(Usuaria usuaria);
}
