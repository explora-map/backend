package explora.map.service;

import explora.map.dto.JwtResponseDTO;
import explora.map.dto.LoginRequestDTO;
import explora.map.dto.RefreshTokenRequestDTO;
import explora.map.dto.RegisterRequestDTO;
import explora.map.entity.RefreshToken;
import explora.map.entity.RolApp;
import explora.map.entity.Usuaria;
import explora.map.repository.UsuariaRepository;
import explora.map.security.AuthEntryPointJwt;
import explora.map.security.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);


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

        usuariaRepository.save(usuaria);
        logger.info("Novo usuario rexistrado: {}", request.getUsername());
    }

    @Transactional
    @Override
    public JwtResponseDTO entrar(LoginRequestDTO request) throws RuntimeException{
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            String username =  authentication.getName();
            Usuaria usuaria = usuariaRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));
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
