package explora.map.service;

import explora.map.dto.JwtResponseDTO;
import explora.map.dto.LoginRequestDTO;
import explora.map.dto.RegisterRequestDTO;
import explora.map.entity.RefreshToken;
import explora.map.entity.RolApp;
import explora.map.entity.TokenVerificacion;
import explora.map.entity.Usuaria;
import explora.map.repository.TokenVerificacionRepository;
import explora.map.repository.UsuariaRepository;
import explora.map.security.AuthEntryPointJwt;
import explora.map.security.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementación do servizo de autenticación.
 *
 * <p>Xestiona o ciclo completo de autenticación de usuarias: rexistro con verificación
 * por correo, inicio de sesión con emisión de tokens JWT, renovación de tokens mediante
 * rotación de refresh token, e peche de sesión con revogación do token.</p>
 *
 * <p>O rexistro crea a usuaria nun estado non verificado ata que confirme o correo.
 * O acceso está bloqueado para contas non verificadas.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UsuariaRepository usuariaRepository;
    private final RefreshTokenService refreshTokenService;
    private final TokenVerificacionRepository tokenVerificacionRepository;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Value("${app.verificacion.expiracion-horas}")
    private int expiracionHoras;


    /**
     * Rexistra unha nova usuaria na plataforma.
     *
     * <p>Crea a conta no estado non verificado e envía un token de verificación
     * ao correo electrónico indicado. O acceso estará bloqueado ata que a usuaria
     * confirme a súa conta.</p>
     *
     * @param request datos de rexistro: nome, username, correo e contrasinal
     * @throws IllegalArgumentException se o username ou o correo xa están en uso
     */
    @Transactional
    @Override
    public void rexistro(RegisterRequestDTO request) {
        if (usuariaRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("O username '" + request.getUsername() + "' xa está en uso.");
        }
        if (usuariaRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("O correo '" + request.getCorreo() + "' xa está rexistrado.");
        }

        Usuaria usuaria = Usuaria.builder()
                .nome(request.getNome())
                .username(request.getUsername())
                .correo(request.getCorreo())
                // BCrypt — nunca gardamos o password en texto plano
                .hashPassword(passwordEncoder.encode(request.getPassword()))
                .rol(RolApp.USER)
                .build();

        Usuaria novaUsuaria = usuariaRepository.save(usuaria);
        logger.info("Novo usuario rexistrado: {}", request.getUsername());

        // Create email verification token
        String token = java.util.UUID.randomUUID().toString();
        TokenVerificacion tokenVerificacion = new TokenVerificacion();
        tokenVerificacion.setToken(token);
        tokenVerificacion.setUsuaria(novaUsuaria);
        tokenVerificacion.setDataExpiracion(LocalDateTime.now().plusHours(expiracionHoras));
        tokenVerificacionRepository.save(tokenVerificacion);
        System.out.println("DEBUG token expiracion: " + tokenVerificacion.getDataExpiracion());
        System.out.println("DEBUG now: " + LocalDateTime.now());

        // Send verification email (async to not block response)
        try {
            emailService.enviarCorreoVerificacion(novaUsuaria.getCorreo(), novaUsuaria.getNome(), token);
        } catch (Exception e) {
            // Log but don't fail registration if email fails
            logger.warn("Non se puido enviar o correo de verificación a {}: {}", novaUsuaria.getCorreo(), e.getMessage());
        }
    }

    /**
     * Autentica unha usuaria e devolve un par de tokens JWT.
     *
     * <p>Valida as credenciais, comproba que a conta estea verificada e xera
     * un access token de curta duración xunto cun refresh token persistido na base de datos.</p>
     *
     * @param request credenciais de acceso: username e contrasinal
     * @return DTO co access token, o refresh token e a data de expiración
     * @throws BadCredentialsException se o username ou o contrasinal son incorrectos
     * @throws IllegalStateException   se a conta da usuaria non está verificada
     */
    @Transactional
    @Override
    public AuthService.LoginResult entrar(LoginRequestDTO request) throws RuntimeException{
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            String username =  authentication.getName();
            Usuaria usuaria = usuariaRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            if (!usuaria.isVerificada()) {
                throw new IllegalStateException("Conta non verificada. Revisa o teu correo electrónico.");
            }

            String accessToken = jwtUtils.generateTokenFromUsername(username);
            RefreshToken refreshToken = refreshTokenService.novo(usuaria);
            LocalDateTime tokenExpiration = jwtUtils.getExpirationDateTime(accessToken);
            JwtResponseDTO dto = JwtResponseDTO.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .tokenExpiration(tokenExpiration)
                    .build();
            return new AuthService.LoginResult(dto, refreshToken.getTokenHash());
        }catch (AuthenticationException e){
            throw new BadCredentialsException("Username ou contrasinal incorrecto.");
        }
    }

    /**
     * Pecha a sesión da usuaria invalidando o seu refresh token.
     *
     * <p>Revoga o refresh token na base de datos, impedindo calquera renovación futura
     * con ese token. O access token seguirá sendo válido ata a súa expiración natural.</p>
     *
     * @param refreshToken contén o refresh token a invalidar
     */
    @Transactional
    @Override
    public void sair(String refreshToken){
        refreshTokenService.revoke(refreshToken);
        logger.info("Logout: refresh token invalidado");
    }

}
