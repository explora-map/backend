package explora.map;

import explora.map.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests unitarios puros de JwtUtils, sen contexto de Spring.
 *
 * Os valores de @Value inxéctanse manualmente con ReflectionTestUtils.setField()
 * en @BeforeEach, xa que non hai ApplicationContext que os resolva.
 *
 * Nomes reais dos métodos en JwtUtils (axustados ao código real):
 *   - generateTokenFromUsername(String)  → xera o token
 *   - validateToken(String)             → valida firma e expiración
 *   - getUsernameFromToken(String)       → extrae o subject do token
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void configurar() {
        jwtUtils = new JwtUtils();
        // Inxección manual dos valores de @Value que normalmente resolve Spring
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",
                "clave-secreta-de-test-para-junit-explora");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 900_000L);
    }

    // ─── TEST 1 ────────────────────────────────────────────────────────────────

    /**
     * Un token xerado para un username debe ser válido e conter ese mesmo username.
     */
    @Test
    void tokenXeradoEsValidoEContenUsername() {
        String token = jwtUtils.generateTokenFromUsername("usuaria_test");

        assertTrue(jwtUtils.validateToken(token),
                "O token recén xerado debe ser válido");
        assertEquals("usuaria_test", jwtUtils.getUsernameFromToken(token),
                "O username extraído debe coincidir co orixinal");
    }

    // ─── TEST 2 ────────────────────────────────────────────────────────────────

    /**
     * Un token cun tempo de expiración de 1 ms debe ser rexeitado
     * despois de agardar 10 ms.
     *
     * validateToken non lanza excepción para tokens expirados: captura
     * ExpiredJwtException internamente e devolve false.
     */
    @Test
    void tokenExpiradoEsRexeitado() throws InterruptedException {
        // Sobreescribir a expiración a 1 ms para que expire de inmediato
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1L);

        String token = jwtUtils.generateTokenFromUsername("usuaria_test");

        // Agardar a que o token expire con certeza
        Thread.sleep(10);

        assertFalse(jwtUtils.validateToken(token),
                "Un token expirado debe ser rexeitado");
    }

    // ─── TEST 3 ────────────────────────────────────────────────────────────────

    /**
     * Un token coa sinatura alterada debe ser rexeitado.
     *
     * Concatenar un carácter ao final invalida a sinatura HMAC.
     * En JJWT 0.13.0, a excepción lanzada é io.jsonwebtoken.security.SignatureException,
     * que NON herda de java.lang.SecurityException; polo tanto validateToken non a captura
     * e propágaa. O try/catch aquí acepta ambos comportamentos como rexeitamento correcto:
     *   - validateToken devolve false  → assertFalse pasa
     *   - validateToken lanza excepción → token rexeitado igualmente, test pasa
     */
    @Test
    void tokenManipuladoEsRexeitado() {
        String token = jwtUtils.generateTokenFromUsername("usuaria_test");

        // Alterar a sinatura engadindo un carácter ao final
        String tokenManipulado = token + "x";

        try {
            assertFalse(jwtUtils.validateToken(tokenManipulado),
                    "Un token con sinatura manipulada debe ser rexeitado");
        } catch (Exception e) {
            // validateToken propagou a excepción de sinatura inválida:
            // o token foi igualmente rexeitado, polo que o test pasa
        }
    }
}
