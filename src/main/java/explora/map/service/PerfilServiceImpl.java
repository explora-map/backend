package explora.map.service;

import explora.map.dto.PerfilRequestDTO;
import explora.map.dto.PerfilResponseDTO;
import explora.map.entity.Mapa;
import explora.map.entity.Usuaria;
import explora.map.repository.ConviteRepository;
import explora.map.repository.MapaGardadoRepository;
import explora.map.repository.MapaMembroRepository;
import explora.map.repository.MapaRepository;
import explora.map.repository.RefreshTokenRepository;
import explora.map.repository.TokenVerificacionRepository;
import explora.map.repository.UsuariaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

    private final UsuariaRepository usuariaRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenVerificacionRepository tokenVerificacionRepository;
    private final MapaGardadoRepository mapaGardadoRepository;
    private final MapaMembroRepository mapaMembroRepository;
    private final ConviteRepository conviteRepository;
    private final MapaRepository mapaRepository;
    private final MapaService mapaService;

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

    @Override
    @Transactional
    public void eliminar(String username) {
        Usuaria usuaria = usuariaRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuaria non encontrada: " + username));

        // a. Elimina todos os refresh tokens
        refreshTokenRepository.deleteByUsuaria(usuaria);

        // b. Elimina o token de verificación de email
        tokenVerificacionRepository.deleteByUsuaria_Id(usuaria.getId());

        // c. Elimina todos os mapas gardados pola usuaria
        mapaGardadoRepository.deleteAllByUsuariaUsername(username);

        // d. Elimina todas as entradas de MapaMembro onde a usuaria é membro
        mapaMembroRepository.deleteAllByUsuariaUsername(username);

        // e. Elimina todos os convites onde é anfitrioa ou convidada
        conviteRepository.deleteByAnfitriaoOrConvidada(usuaria);

        // f. Elimina cada mapa propiedade da usuaria (con todo o seu contido en cascada)
        List<Mapa> mapas = mapaRepository.findByCreadoPor(username);
        for (Mapa mapa : mapas) {
            mapaService.eliminar(mapa.getId(), username);
        }

        // g. Elimina a entidade Usuaria
        usuariaRepository.delete(usuaria);
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
