package explora.map;

import explora.map.entity.Mapa;
import explora.map.entity.TipoMapa;
import explora.map.repository.MapaMembroRepository;
import explora.map.service.MapaAccesoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios puros de MapaAccesoService, sen contexto de Spring.
 *
 * Adaptacións ao código real:
 *  - O método é verificar(Mapa mapa, String username), non verificar(Long, String).
 *    Recibe a entidade Mapa directamente, polo que non se precisa MapaRepository.
 *  - A única dependencia de MapaAccesoService é MapaMembroRepository.
 *  - O mock usa existsByMapaIdAndUsuariaUsername (boolean), non findBy...
 */
@ExtendWith(MockitoExtension.class)
class MapaAccesoServiceTest {

    @Mock
    private MapaMembroRepository mapaMembroRepository;

    @InjectMocks
    private MapaAccesoService mapaAccesoService;

    private Mapa mapaPrivado;
    private Mapa mapaPublico;

    @BeforeEach
    void configurar() {
        // Mapa PRIVADO con id=1L, creado pola "propietaria"
        mapaPrivado = new Mapa();
        mapaPrivado.setId(1L);
        mapaPrivado.setTipo(TipoMapa.PRIVADO);
        mapaPrivado.setCreadoPor("propietaria");

        // Mapa PUBLICO con id=2L, creado por "outra"
        mapaPublico = new Mapa();
        mapaPublico.setId(2L);
        mapaPublico.setTipo(TipoMapa.PUBLICO);
        mapaPublico.setCreadoPor("outra");

        // Os stubs de mapaMembroRepository defínense en cada test que os necesita,
        // para evitar UnnecessaryStubbing co strict mode de MockitoExtension:
        // os tests 1 e 2 retornan antes de chamar ao repositorio.
    }

    // ─── TEST 1 ────────────────────────────────────────────────────────────────

    /**
     * Un mapa público é accesible por calquera usuaria, sen comprobar membresía.
     */
    @Test
    void mapaPublicoCalqueraUsuariaTenAcceso() {
        assertDoesNotThrow(
                () -> mapaAccesoService.verificar(mapaPublico, "calquera"),
                "Un mapa público non debe lanzar excepción para ningunha usuaria"
        );
    }

    // ─── TEST 2 ────────────────────────────────────────────────────────────────

    /**
     * A propietaria (creadoPor) sempre ten acceso ao seu mapa privado,
     * sen necesidade de entrada en MapaMembro.
     */
    @Test
    void mapaPrivadoPropietariaTenAcceso() {
        assertDoesNotThrow(
                () -> mapaAccesoService.verificar(mapaPrivado, "propietaria"),
                "A propietaria non debe ser bloqueada no seu propio mapa privado"
        );
    }

    // ─── TEST 3 ────────────────────────────────────────────────────────────────

    /**
     * Unha usuaria con entrada en MapaMembro pode acceder ao mapa privado.
     */
    @Test
    void mapaPrivadoMembroConEntradaTenAcceso() {
        // "membro1" ten entrada no mapa privado
        when(mapaMembroRepository.existsByMapaIdAndUsuariaUsername(1L, "membro1"))
                .thenReturn(true);

        assertDoesNotThrow(
                () -> mapaAccesoService.verificar(mapaPrivado, "membro1"),
                "Unha usuaria con entrada en MapaMembro non debe ser bloqueada"
        );
    }

    // ─── TEST 4 ────────────────────────────────────────────────────────────────

    /**
     * Unha usuaria foránea (sen entrada en MapaMembro nin creadoPor) non pode
     * acceder a un mapa privado: debe lanzarse IllegalStateException.
     */
    @Test
    void mapaPrivadoForaneaSenEntradaLanzaExcepcion() {
        // "foranea" non ten entrada no mapa privado
        when(mapaMembroRepository.existsByMapaIdAndUsuariaUsername(1L, "foranea"))
                .thenReturn(false);

        assertThrows(
                IllegalStateException.class,
                () -> mapaAccesoService.verificar(mapaPrivado, "foranea"),
                "Unha usuaria foránea nun mapa privado debe recibir IllegalStateException"
        );
    }
}
