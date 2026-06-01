package explora.map.security;

import explora.map.entity.Usuaria;
import explora.map.repository.UsuariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de UserDetailsService que carga a usuaria desde a base de datos polo username. */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UsuariaRepository usuariaRepository;

    /**
     * Carga os datos dunha usuaria desde a base de datos polo seu username.
     *
     * @param username nome de usuaria a buscar
     * @return instancia de {@link UserDetails} coa información da usuaria autenticada
     * @throws UsernameNotFoundException se non existe ningunha usuaria co username dado
     */
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
