package explora.map.service;

import explora.map.dto.PerfilRequestDTO;
import explora.map.dto.PerfilResponseDTO;
import explora.map.entity.Usuaria;
import explora.map.repository.UsuariaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

    private final UsuariaRepository usuariaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PerfilResponseDTO obterPerfil(String username) {
        Usuaria usuaria = usuariaRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuaria non encontrada: " + username));
        return mapearAResponse(usuaria);
    }

    @Override
    @Transactional
    public PerfilResponseDTO actualizarPerfil(String username, PerfilRequestDTO dto) {
        Usuaria usuaria = usuariaRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuaria non encontrada: " + username));

        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            usuaria.setNome(dto.getNome());
        }

        if (dto.getUsername() != null && !dto.getUsername().isBlank()
                && !dto.getUsername().equals(usuaria.getUsername())) {
            if (usuariaRepository.existsByUsername(dto.getUsername())) {
                throw new IllegalArgumentException("O nome de usuaria xa está en uso.");
            }
            usuaria.setUsername(dto.getUsername());
        }

        if (dto.getCorreo() != null && !dto.getCorreo().isBlank()
                && !dto.getCorreo().equals(usuaria.getCorreo())) {
            if (usuariaRepository.existsByCorreo(dto.getCorreo())) {
                throw new IllegalArgumentException("O correo electrónico xa está en uso.");
            }
            usuaria.setCorreo(dto.getCorreo());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuaria.setHashPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getIdioma() != null
                && (dto.getIdioma().equals("gl") || dto.getIdioma().equals("en"))) {
            usuaria.setIdioma(dto.getIdioma());
        }

        usuariaRepository.save(usuaria);
        return mapearAResponse(usuaria);
    }

    // Maps Usuaria entity to PerfilResponseDTO
    private PerfilResponseDTO mapearAResponse(Usuaria usuaria) {
        return PerfilResponseDTO.builder()
            .id(usuaria.getId())
            .nome(usuaria.getNome())
            .username(usuaria.getUsername())
            .correo(usuaria.getCorreo())
            .rol(usuaria.getRol().name())
            .idioma(usuaria.getIdioma())
            .dataCreacion(usuaria.getDataCreacion() != null
                ? usuaria.getDataCreacion().toString() : null)
            .build();
    }
}
