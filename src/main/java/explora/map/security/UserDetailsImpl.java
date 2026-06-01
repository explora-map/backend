package explora.map.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import explora.map.entity.Usuaria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Implementación de UserDetails que encapsula os datos da Usuaria para Spring Security. */
@AllArgsConstructor
@Getter
public class UserDetailsImpl implements UserDetails{
        private Long id;
        private String username;
        private String correo;

        @JsonIgnore
        private String password;

        private Collection<? extends GrantedAuthority> authorities;

        /**
         * Constrúe un {@code UserDetailsImpl} a partir dunha entidade {@link explora.map.entity.Usuaria}.
         *
         * @param user entidade usuaria obtida da base de datos
         * @return instancia de {@code UserDetailsImpl} lista para Spring Security
         */
        public static UserDetailsImpl build(Usuaria user) {
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRol().name()));
                return new UserDetailsImpl(
                        user.getId(),
                        user.getUsername(),
                        user.getCorreo(),
                        user.getHashPassword(),
                        authorities);
        }

        /**
         * Indica se a conta non está caducada. Sempre devolve {@code true} nesta implementación.
         *
         * @return {@code true} en todo caso
         */
        @Override
        public boolean isAccountNonExpired() {
                return true;
        }

        @Override
        public boolean isAccountNonLocked() {
                return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
                return true;
        }

        /**
         * Indica se a conta está habilitada. Sempre devolve {@code true} nesta implementación.
         *
         * @return {@code true} en todo caso
         */
        @Override
        public boolean isEnabled() {
                return true;
        }
}
