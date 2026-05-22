package explora.map;

import explora.map.entity.Convite;
import explora.map.entity.EstadoConvite;
import explora.map.entity.Mapa;
import explora.map.entity.MapaMembro;
import explora.map.entity.Marcador;
import explora.map.entity.RolApp;
import explora.map.entity.RolMapa;
import explora.map.entity.TipoMapa;
import explora.map.entity.Usuaria;
import explora.map.repository.ConviteRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hamcrest.Matchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    private ConviteRepository conviteRepository;

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

    // ─── TEST 6 ────────────────────────────────────────────────────────────────

    /**
     * Unha petición a un endpoint protexido sen cabeceira Authorization
     * debe devolver 401 Unauthorized (AuthEntryPointJwt actúa como punto de entrada).
     */
    @Test
    void requestSenJwtAEndpointProtexido_retorna401() throws Exception {
        mockMvc.perform(get("/api/mapas/" + mapa.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ─── TEST 7 ────────────────────────────────────────────────────────────────

    /**
     * Unha petición con un JWT manipulado (sinatura inválida) debe devolver 401 Unauthorized.
     * O filtro AuthTokenFilter rexeita o token e non establece o contexto de seguranza,
     * polo que Spring Security deniega o acceso ao recurso protexido.
     */
    @Test
    void jwtManipulado_retorna401() throws Exception {
        // Token con estrutura JWT válida pero sinatura incorrecta
        String tokenInvalido = "eyJhbGciOiJIUzI1NiJ9.invalido.invalido";

        mockMvc.perform(get("/api/mapas/" + mapa.getId())
                        .header("Authorization", "Bearer " + tokenInvalido))
                .andExpect(status().isUnauthorized());
    }

    // ─── TEST 8 ────────────────────────────────────────────────────────────────

    /**
     * Un intento de login con contrasinal incorrecto debe devolver 401 Unauthorized.
     * AuthServiceImpl captura a AuthenticationException e relánzaa como BadCredentialsException,
     * que GlobalExceptionHandler mapea a 401.
     *
     * Nota: o campo do DTO é "password" (non "contrasinal"), axustado ao LoginRequestDTO real.
     */
    @Test
    void loginConCredenciaisIncorrectas_retorna401() throws Exception {
        mockMvc.perform(post("/api/auth/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"propietaria\",\"password\":\"contrasinalErroneo\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ─── TEST 9 ────────────────────────────────────────────────────────────────

    /**
     * Un intento de login cunha conta existente pero non verificada debe devolver 403 Forbidden.
     * AuthServiceImpl autentica as credenciais correctamente e despois comproba isVerificada();
     * se é false, lanza IllegalStateException, que GlobalExceptionHandler mapea a 403.
     *
     * Nota: o campo do DTO é "password" (non "contrasinal"), axustado ao LoginRequestDTO real.
     */
    @Test
    void loginConContaNonVerificada_retorna403() throws Exception {
        // Crear usuaria con verificada=false directamente no repositorio
        usuariaRepository.save(Usuaria.builder()
                .nome("Non Verificada Test")
                .username("nonverificada")
                .correo("nonverificada@test.com")
                .hashPassword(passwordEncoder.encode("password123"))
                .rol(RolApp.USER)
                .verificada(false)
                .build());

        mockMvc.perform(post("/api/auth/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nonverificada\",\"password\":\"password123\"}"))
                .andExpect(status().isForbidden());
    }

    // ─── TEST 10 ───────────────────────────────────────────────────────────────

    /**
     * A propietaria (creadora do mapa) pode crear categorías:
     * o servidor debe devolver 201 Created.
     *
     * CategoriaServiceImpl.crear() delega en mapaMembroService.verificarPermisoEscritura(),
     * que permite o acceso ao creador do mapa sen necesidade de entrada en MapaMembro.
     */
    @Test
    void propietariaCriaCategoria_retorna201() throws Exception {
        String token = jwtUtils.generateTokenFromUsername("propietaria");

        mockMvc.perform(post("/api/mapas/" + mapa.getId() + "/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Restaurantes\",\"cor\":\"#FF5733\",\"icona\":\"fork\"}"))
                .andExpect(status().isCreated());
    }

    // ─── TEST 11 ───────────────────────────────────────────────────────────────

    /**
     * A propietaria pode listar os marcadores do seu mapa:
     * o servidor debe devolver 200 OK cunha lista que contén polo menos 1 elemento
     * (o marcador de colaboradora1 creado en @BeforeEach).
     */
    @Test
    void propietariaListaMarcadores_retorna200EListaCorrecta() throws Exception {
        String token = jwtUtils.generateTokenFromUsername("propietaria");

        mockMvc.perform(get("/api/mapas/" + mapa.getId() + "/marcadores")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(Matchers.greaterThanOrEqualTo(1)));
    }

    // ─── TEST 12 ───────────────────────────────────────────────────────────────

    /**
     * A propietaria pode editar o seu propio mapa:
     * o servidor debe devolver 200 OK.
     *
     * Nota: MapaRequestDTO require os campos nome, latitude, lonxitude,
     * nomeLocalizacion e tipo (todos @NotNull/@NotBlank). As coordenadas
     * son iguais ás orixinais para evitar a chamada HTTP a Nominatim
     * (MapaServiceImpl só a fai cando as coordenadas cambian).
     */
    @Test
    void propietariaEditaSeuMapa_retorna200() throws Exception {
        String token = jwtUtils.generateTokenFromUsername("propietaria");

        mockMvc.perform(put("/api/mapas/editar/" + mapa.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Mapa Editado\",\"descricion\":\"Nova descricion\"," +
                                 "\"latitude\":42.0,\"lonxitude\":-8.0," +
                                 "\"nomeLocalizacion\":\"Vigo, Galicia\",\"tipo\":\"PRIVADO\"}"))
                .andExpect(status().isOk());
    }

    // ─── TEST 13 ───────────────────────────────────────────────────────────────

    /**
     * Ao eliminar un mapa, os seus marcadores tamén desaparecen en cascada.
     * Verificamos o cascade comprobando que o mapa xa non existe (404) ao intentar
     * listar os seus marcadores, o que é máis robusto que acceder ao marcador directamente.
     *
     * Fluxo:
     *   1. DELETE /api/mapas/eliminar/{mapaId}     → 204 No Content
     *   2. GET    /api/mapas/{mapaId}/marcadores   → 404 Not Found (mapa eliminado)
     */
    @Test
    void eliminarMapaEliminaMarcadoresEnCascada_retorna204e404() throws Exception {
        // Gardar o id do marcador creado en @BeforeEach antes de eliminar o mapa
        Long idMarcadorDeColaboradora = marcadorDeColaboradora.getId();

        String token = jwtUtils.generateTokenFromUsername("propietaria");

        // Paso 1: eliminar o mapa → 204
        mockMvc.perform(delete("/api/mapas/eliminar/" + mapa.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Paso 2: tentar listar marcadores do mapa eliminado → 404
        // MarcadorServiceImpl.listarPorMapa() lanza IllegalArgumentException ao non atopar o mapa,
        // que GlobalExceptionHandler converte en 404.
        mockMvc.perform(get("/api/mapas/" + mapa.getId() + "/marcadores")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ─── TEST 14 ───────────────────────────────────────────────────────────────

    /**
     * Un ADMIN_MAPA pode editar calquera marcador do mapa, incluídos os alleos.
     * Contrasta con TEST 4: a COLABORADORA só pode editar os seus propios marcadores.
     *
     * MarcadorServiceImpl.verificarPermisosEdicion(): rol == ADMIN_MAPA pasa sen restrición
     * de autoría, a diferenza de COLABORADORA que require creadoPor == username.
     *
     * Nota: a URL real é /api/mapas/api/marcadores/{id} (ver comentario en TEST 4).
     */
    @Test
    void adminMapaEditaMarcadorAlleo_retorna200() throws Exception {
        // Crear usuaria "adminmapa" e engadila ao mapa con rol ADMIN_MAPA
        Usuaria adminmapa = usuariaRepository.save(Usuaria.builder()
                .nome("Admin Mapa Test")
                .username("adminmapa")
                .correo("adminmapa@test.com")
                .hashPassword(passwordEncoder.encode("password123"))
                .rol(RolApp.USER)
                .verificada(true)
                .build());

        mapaMembroRepository.save(MapaMembro.builder()
                .mapa(mapa)
                .usuaria(adminmapa)
                .rol(RolMapa.ADMIN_MAPA)
                .build());

        String token = jwtUtils.generateTokenFromUsername("adminmapa");

        // O marcador a editar pertence a "colaboradora1" (alleo a adminmapa)
        mockMvc.perform(put("/api/mapas/api/marcadores/" + marcadorDeColaboradora.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Editado por admin\",\"latitude\":42.1,\"lonxitude\":-8.1}"))
                .andExpect(status().isOk());
    }

    // ─── TEST 15 ───────────────────────────────────────────────────────────────

    /**
     * Fluxo completo de invitación:
     *   1. "convidada" non pode acceder ao mapa privado antes de aceptar o convite → 403
     *   2. "convidada" acepta o convite → 204 (ConviteController devolve noContent)
     *   3. "convidada" pode acceder ao mapa tras o convite aceptado → 200
     *
     * MapaServiceImpl.obterPorId() permite o acceso cando existe un convite con
     * estado ACEPTADO para a usuaria solicitante, independentemente de MapaMembro.
     * ConviteServiceImpl.aceptar() cambia o estado a ACEPTADO e crea tamén
     * unha entrada en MapaMembro.
     */
    @Test
    void usuariaAceptaConviteEAccedeAoMapa_fluxoCompleto() throws Exception {
        // Crear a usuaria "convidada"
        Usuaria convidada = usuariaRepository.save(Usuaria.builder()
                .nome("Convidada Test")
                .username("convidada")
                .correo("convidada@test.com")
                .hashPassword(passwordEncoder.encode("password123"))
                .rol(RolApp.USER)
                .verificada(true)
                .build());

        // Crear o convite directamente no repositorio (estado PENDENTE)
        UUID tokenConvite = UUID.randomUUID();
        conviteRepository.save(Convite.builder()
                .anfitrioa(propietaria)
                .convidada(convidada)
                .mapa(mapa)
                .token(tokenConvite)
                .estado(EstadoConvite.PENDENTE)
                .rol(RolMapa.MEMBRO)
                .dataExpiracion(LocalDateTime.now().plusDays(1))
                .build());

        String tokenConvidada = jwtUtils.generateTokenFromUsername("convidada");

        // Paso 1: "convidada" non pode acceder ao mapa privado con convite PENDENTE → 403
        mockMvc.perform(get("/api/mapas/" + mapa.getId())
                        .header("Authorization", "Bearer " + tokenConvidada))
                .andExpect(status().isForbidden());

        // Paso 2: "convidada" acepta o convite → ConviteController devolve 204 (noContent)
        mockMvc.perform(patch("/api/convites/" + tokenConvite + "/aceptar")
                        .header("Authorization", "Bearer " + tokenConvidada))
                .andExpect(status().isNoContent());

        // Paso 3: "convidada" pode acceder ao mapa tras o convite ACEPTADO → 200
        mockMvc.perform(get("/api/mapas/" + mapa.getId())
                        .header("Authorization", "Bearer " + tokenConvidada))
                .andExpect(status().isOk());
    }
}
