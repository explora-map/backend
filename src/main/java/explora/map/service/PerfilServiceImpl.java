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

/**
 * Implementación de {@link PerfilService} que xestiona o perfil das usuarias.
 *
 * <p>Responsabilidades principais:</p>
 * <ul>
 *   <li>Consulta e actualización dos datos persoais da usuaria autenticada.</li>
 *   <li>Eliminación completa da conta: tokens, mapas gardados, membresías,
 *       convites e mapas propios en cascada.</li>
 * </ul>
 */
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

    /**
     * Obtén os datos do perfil da usuaria autenticada.
     *
     * @param username nome da usuaria cuio perfil se solicita
     * @return DTO co perfil da usuaria (nome, username, correo, rol, idioma e data de creación)
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException
     *         se non existe ningunha usuaria co username indicado
     */
    @Override
    public PerfilResponseDTO obterPerfil(String username) {
        Usuaria usuaria = usuariaRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuaria non encontrada: " + username));
        return mapearAResponse(usuaria);
    }

    /**
     * Actualiza os datos do perfil da usuaria autenticada.
     *
     * <p>Só se modifican os campos presentes e non baleiros no DTO.
     * O contrasinal gárdase sempre cifrado con BCrypt.
     * O idioma só se acepta se é {@code "gl"} ou {@code "en"}.</p>
     *
     * @param username nome actual da usuaria que realiza a actualización
     * @param dto      datos a actualizar (nome, username, correo, contrasinal, idioma);
     *                 os campos {@code null} ou baleiros ignóranse
     * @return DTO co perfil actualizado
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException
     *         se non existe ningunha usuaria co username indicado
     * @throws IllegalArgumentException se o novo username ou correo xa está en uso
     *                                  por outra usuaria
     */
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

        String contrasinelNovo = dto.getContrasinelNovo();
        boolean quereChangar = contrasinelNovo != null && !contrasinelNovo.isBlank();
        if (quereChangar) {
            String contrasinelActual = dto.getContrasinelActual();
            if (contrasinelActual == null || contrasinelActual.isBlank()) {
                throw new IllegalArgumentException("Debes indicar o contrasinal actual.");
            }
            if (!passwordEncoder.matches(contrasinelActual, usuaria.getHashPassword())) {
                throw new IllegalArgumentException("O contrasinal actual é incorrecto.");
            }
            if (contrasinelNovo.equals(contrasinelActual)) {
                throw new IllegalArgumentException("O novo contrasinal debe ser diferente ao actual.");
            }
            if (!contrasinelNovo.equals(dto.getContrasinelNovoConfirmacion())) {
                throw new IllegalArgumentException("Os contrasineis novos non coinciden.");
            }
            usuaria.setHashPassword(passwordEncoder.encode(contrasinelNovo));
        }

        if (dto.getIdioma() != null
                && (dto.getIdioma().equals("gl") || dto.getIdioma().equals("en"))) {
            usuaria.setIdioma(dto.getIdioma());
        }

        usuariaRepository.save(usuaria);
        return mapearAResponse(usuaria);
    }

    /**
     * Elimina de forma permanente a conta da usuaria e todos os seus datos asociados.
     *
     * <p>A eliminación realízase na seguinte orde para respectar as restricións
     * de integridade referencial:</p>
     * <ol>
     *   <li>Refresh tokens da usuaria.</li>
     *   <li>Token de verificación de correo electrónico.</li>
     *   <li>Mapas gardados (favoritos).</li>
     *   <li>Entradas de {@code MapaMembro} onde a usuaria é membro.</li>
     *   <li>Convites onde é anfitrioa ou convidada.</li>
     *   <li>Mapas propios da usuaria (con marcadores, categorías e historial en cascada).</li>
     *   <li>Entidade {@code Usuaria}.</li>
     * </ol>
     *
     * @param username nome da usuaria cuia conta se vai eliminar
     * @throws IllegalArgumentException se non existe ningunha usuaria co username indicado
     */
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
