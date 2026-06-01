package explora.map.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Configuración de JPA Auditing. Activa @EnableJpaAuditing e provee o AuditorAware co username da usuaria autenticada. */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(
                SecurityContextHolder.getContext().getAuthentication()
        ).filter(Authentication::isAuthenticated)
         .filter(auth -> !"anonymousUser".equals(auth.getName()))
         .map(Authentication::getName);
    }
}
