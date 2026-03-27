package explora.map.security;

import explora.map.entity.Usuaria;
import explora.map.repository.UsuariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UsuariaRepository usuariaRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Usuaria usuaria = usuariaRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario non encontrado: " + username
                        )
                );

        // Construímos un UserDetails estándar de Spring Security.
        // Para este sprint non hai roles, polo que asignamos ROLE_USER.
        return org.springframework.security.core.userdetails.User
                .withUsername(usuaria.getUsername())
                .password(usuaria.getHashPassword())
                .roles("USER")
                .build();
    }
}
