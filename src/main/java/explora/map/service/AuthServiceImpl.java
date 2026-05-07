package explora.map.service;

import explora.map.dto.JwtResponseDTO;
import explora.map.dto.LoginRequestDTO;
import explora.map.dto.RefreshTokenRequestDTO;
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

    @Transactional
    @Override
    public JwtResponseDTO entrar(LoginRequestDTO request) throws RuntimeException{
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
            return new JwtResponseDTO(accessToken, refreshToken.getTokenHash(), "Bearer", tokenExpiration);
        }catch (AuthenticationException e){
            throw new BadCredentialsException("Username ou contrasinal incorrecto.");
        }
    }

    @Transactional
    @Override
    public void sair(RefreshTokenRequestDTO request){
        refreshTokenService.revoke(request.getRefreshToken());
        logger.info("Logout: refresh token invalidado");
    }

}
