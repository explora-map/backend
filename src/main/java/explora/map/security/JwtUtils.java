package explora.map.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/** Utilidade para xeración, validación e extracción de datos de tokens JWT. */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access.expiracion}")
    private Long jwtExpirationMs;

    private SecretKey getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            return Keys.hmacShaKeyFor(jwtSecret.getBytes());
        }
    }

    /**
     * Xera un JWT asinado co username como suxeito.
     *
     * @param username nome de usuaria a codificar no token
     * @return token JWT asinado en formato compacto Base64
     */
    public String generateTokenFromUsername(String username) {
        Date now    = new Date();
        Date expiration = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valida a sinatura e a vixencia do token JWT.
     *
     * @param token string JWT a validar
     * @return {@code true} se o token é válido e non expirou; {@code false} en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT expirado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("JWT malformado: {}", e.getMessage());
        } catch (SecurityException e) {
            logger.warn("Sinatura JWT inválida: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("JWT baleiro ou nulo: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extrae o username do payload dun token JWT válido.
     *
     * @param token string JWT do que se extrae o suxeito
     * @return nome de usuaria codificado no campo {@code sub} do token
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Calcula a data e hora de expiración para un token xerado no momento actual.
     *
     * @return data e hora de expiración en fuso horario do sistema
     */
    public LocalDateTime calculateExpirationDateTime() {
        return LocalDateTime.now().plusSeconds(jwtExpirationMs / 1000);
    }

    /**
     * Obtén a data e hora de expiración dun token JWT existente.
     *
     * @param token string JWT do que se extrae a data de expiración
     * @return data e hora de expiración contida no payload do token
     */
    public LocalDateTime getExpirationDateTime(String token) {
        Date expiry = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return expiry.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
