package explora.map;

import explora.map.entity.Mapa;
import explora.map.entity.MapaMembro;
import explora.map.entity.Marcador;
import explora.map.entity.RolApp;
import explora.map.entity.RolMapa;
import explora.map.entity.TipoMapa;
import explora.map.entity.Usuaria;
import explora.map.repository.MapaMembroRepository;
import explora.map.repository.MapaRepository;
import explora.map.repository.MarcadorRepository;
import explora.map.repository.UsuariaRepository;
import explora.map.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para verificar o sistema de permisos nos endpoints de mapas e marcadores.
 *
 * Cada test execútase nunha transacción independente con rollback automático ao final,
 * polo que o estado da base de datos H2 queda limpo entre tests.
 *
 * En Spring Boot 4.x, @AutoConfigureMockMvc foi eliminado; o MockMvc configúrase
 * manualmente con MockMvcBuilders.webAppContextSetup(...).apply(springSecurity()).
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class PermisosIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private UsuariaRepository usuariaRepository;

    @Autowired
    private MapaRepository mapaRepository;

    @Autowired
    private MarcadorRepository marcadorRepository;

    @Autowired
    private MapaMembroRepository mapaMembroRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // MockMvc configurado con Spring Security para procesar cabeceiras JWT
    private MockMvc mockMvc;

    // Entidades compartidas entre os tests
    private Usuaria propietaria;
    private Usuaria colaboradora1;
    private Usuaria membro1;
    private Mapa mapa;
    private Marcador marcadorDeColaboradora;

    /**
     * Configura o MockMvc e o contexto de test antes de cada método:
     *  - MockMvc co filtro de Spring Security activo
     *  - 3 usuarias: propietaria, colaboradora1, membro1
     *  - 1 mapa PRIVADO creado pola propietaria
     *  - membresías de colaboradora1 (COLABORADORA) e membro1 (MEMBRO)
     *  - 1 marcador existente creado por colaboradora1 (para tests de eliminación)
     */
    @BeforeEach
    void configurarContexto() {
        // Configurar MockMvc con Spring Security para que o filtro JWT actúe nas peticións
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // ── Crear as 3 usuarias base ──────────────────────────────────────────
        propietaria = usuariaRepository.save(Usuaria.builder()
                .nome("Propietaria Test")
                .username("propietaria")
                .correo("propietaria@test.com")
                .hashPassword(passwordEncoder.encode("password123"))
                .rol(RolApp.USER)
                .verificada(true)
                .build());

        colaboradora1 = usuariaRepository.save(Usuaria.builder()
                .nome("Colaboradora Test")
                .username("colaboradora1")
                .correo("colaboradora1@test.com")
                .hashPassword(passwordEncoder.encode("password123"))
                .rol(RolApp.USER)
                .verificada(true)
                .build());

        membro1 = usuariaRepository.save(Usuaria.builder()
                .nome("Membro Test")
                .username("membro1")
                .correo("membro1@test.com")
                .hashPassword(passwordEncoder.encode("password123"))
                .rol(RolApp.USER)
                .verificada(true)
                .build());

        // ── Crear mapa PRIVADO co contexto de seguranza da propietaria ────────
        // O contexto é necesario para que @CreatedBy en Mapa.creadoPor se
        // resolva correctamente a través de AuditorAware.
        setarContextoSeguranza("propietaria");
        mapa = mapaRepository.save(Mapa.builder()
                .nome("Mapa Test de Permisos")
                .tipo(TipoMapa.PRIVADO)
                .latitude(42.0)
                .lonxitude(-8.0)
                .nomeLocalizacion("Vigo, Galicia")
                .creadoPor("propietaria") // safety net para AuditorAware baleiro
                .build());

        // ── Engadir membresías ao mapa ────────────────────────────────────────
        mapaMembroRepository.save(MapaMembro.builder()
                .mapa(mapa)
                .usuaria(colaboradora1)
                .rol(RolMapa.COLABORADORA)
                .build());

        mapaMembroRepository.save(MapaMembro.builder()
                .mapa(mapa)
                .usuaria(membro1)
                .rol(RolMapa.MEMBRO)
                .build());

        // ── Crear marcador de colaboradora1 (para tests de eliminación) ───────
        setarContextoSeguranza("colaboradora1");
        marcadorDeColaboradora = marcadorRepository.save(Marcador.builder()
                .nome("Marcador da Colaboradora")
                .latitude(42.1)
                .lonxitude(-8.1)
                .mapa(mapa)
                .creadoPor("colaboradora1") // safety net para AuditorAware baleiro
                .build());

        // Limpar contexto ao rematar o setup; cada test establece o seu propio
        // mediante o JWT enviado na cabeceira Authorization.
        SecurityContextHolder.clearContext();
    }

    /**
     * Establece un contexto de seguranza simulado no SecurityContextHolder.
     * Úsase unicamente para operacións directas ao repositorio (fóra de MockMvc)
     * onde o filtro JWT non actúa, de xeito que @CreatedBy se resolva correctamente.
     */
    private void setarContextoSeguranza(String username) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ─── TEST 1 ────────────────────────────────────────────────────────────────

    /**
     * Unha usuaria sen membresía (nin convite aceptado) nun mapa privado
     * non pode acceder ao mesmo: o servidor debe devolver 403 Forbidden.
     */
    @Test
    void accesoMapaPrivadoSenSerMembro_retorna403() throws Exception {
        // Crear a 4ª usuaria "foranea" (non membro do mapa, sen convite)
        Usuaria foranea = usuariaRepository.save(Usuaria.builder()
                .nome("Foranea Test")
                .username("foranea")
                .correo("foranea@test.com")
                .hashPassword(passwordEncoder.encode("password123"))
                .rol(RolApp.USER)
                .verificada(true)
                .build());

        String token = jwtUtils.generateTokenFromUsername("foranea");

        mockMvc.perform(get("/api/mapas/" + mapa.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ─── TEST 2 ────────────────────────────────────────────────────────────────

    /**
     * Un membro con rol MEMBRO non ten permiso de escritura no mapa:
     * o intento de crear un marcador debe devolver 403 Forbidden.
     */
    @Test
    void membroIntentaCrearMarcador_retorna403() throws Exception {
        String token = jwtUtils.generateTokenFromUsername("membro1");

        mockMvc.perform(post("/api/mapas/" + mapa.getId() + "/marcadores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo\",\"latitude\":42.0,\"lonxitude\":-8.0}"))
                .andExpect(status().isForbidden());
    }

    // ─── TEST 3 ────────────────────────────────────────────────────────────────

    /**
     * Unha colaboradora con rol COLABORADORA ten permiso de escritura no mapa:
     * a creación dun marcador debe devolver 201 Created.
     */
    @Test
    void colaboradoraCraMarcador_retorna201() throws Exception {
        String token = jwtUtils.generateTokenFromUsername("colaboradora1");

        mockMvc.perform(post("/api/mapas/" + mapa.getId() + "/marcadores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo\",\"latitude\":42.0,\"lonxitude\":-8.0}"))
                .andExpect(status().isCreated());
    }

    // ─── TEST 4 ────────────────────────────────────────────────────────────────

    /**
     * Unha colaboradora non pode eliminar un marcador creado por outra usuaria:
     * as colaboradoras só teñen permiso sobre os seus propios elementos.
     * O servidor debe devolver 403 Forbidden.
     *
     * Nota: a URL real do endpoint de eliminación é /api/mapas/api/marcadores/{id}
     * porque MarcadorController ten @RequestMapping("/api/mapas") a nivel de clase
     * e @DeleteMapping("/api/marcadores/{id}") a nivel de método.
     */
    @Test
    void colaboradoraEliminaMarcadorAlleo_retorna403() throws Exception {
        // Crear un segundo marcador cuxa creadoPor = "propietaria" (alleo á colaboradora)
        setarContextoSeguranza("propietaria");
        Marcador marcadorAlleo = marcadorRepository.save(Marcador.builder()
                .nome("Marcador da Propietaria")
                .latitude(42.2)
                .lonxitude(-8.2)
                .mapa(mapa)
                .creadoPor("propietaria")
                .build());
        SecurityContextHolder.clearContext();

        String token = jwtUtils.generateTokenFromUsername("colaboradora1");

        mockMvc.perform(delete("/api/mapas/api/marcadores/" + marcadorAlleo.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ─── TEST 5 ────────────────────────────────────────────────────────────────

    /**
     * A propietaria (creadora do mapa) pode eliminar calquera marcador,
     * incluídos os creados por colaboradoras: o servidor debe devolver 204 No Content.
     *
     * Nota: a URL real do endpoint de eliminación é /api/mapas/api/marcadores/{id}
     * (ver comentario en test 4).
     */
    @Test
    void propietariaEliminaCalqueraMarkador_retorna204() throws Exception {
        // O marcador existente foi creado por "colaboradora1" en @BeforeEach
        String token = jwtUtils.generateTokenFromUsername("propietaria");

        mockMvc.perform(delete("/api/mapas/api/marcadores/" + marcadorDeColaboradora.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
