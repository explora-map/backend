package explora.map.repository;

import explora.map.entity.Usuaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuariaRepository extends JpaRepository<Usuaria, Long> {
    Optional<Usuaria> findByUsername(String username);
    Optional<Usuaria> findByCorreo(String correo);
    Boolean existsByUsername(String username);
    Boolean existsByCorreo(String correo);
}
