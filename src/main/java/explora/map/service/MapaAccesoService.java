package explora.map.service;

import explora.map.entity.Mapa;
import explora.map.entity.TipoMapa;
import explora.map.repository.MapaMembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servizo de verificación de acceso de lectura a mapas.
 *
 * <p>Centraliza a lóxica de control de acceso para calquera operación que requira
 * comprobar se unha usuaria ten permiso para ver un mapa. Úsase desde os servizos
 * de marcadores e categorías antes de devolver datos asociados ao mapa.</p>
 */
@Service
@RequiredArgsConstructor
public class MapaAccesoService {

    private final MapaMembroRepository mapaMembroRepository;

    /**
     * Verifica que a usuaria indicada ten permiso de lectura sobre o mapa.
     *
     * <p>O acceso concédese se se cumpre calquera destas condicións, en orde:</p>
     * <ol>
     *   <li>O mapa é de tipo {@link explora.map.entity.TipoMapa#PUBLICO}.</li>
     *   <li>A usuaria é a creadora do mapa ({@code creadoPor}).</li>
     *   <li>A usuaria ten unha entrada en {@code MapaMembro} para este mapa.</li>
     * </ol>
     *
     * @param mapa     o mapa ao que se quere acceder; non pode ser {@code null}
     * @param username o nome de usuario de quen solicita o acceso; non pode ser {@code null}
     * @throws IllegalStateException se o mapa é privado e a usuaria non é creadora
     *                               nin ten entrada en {@code MapaMembro}
     */
    public void verificar(Mapa mapa, String username) {
        if (mapa.getTipo() == TipoMapa.PUBLICO) {
            return;
        }
        if (mapa.getCreadoPor().equals(username)) {
            return;
        }
        if (mapaMembroRepository.existsByMapaIdAndUsuariaUsername(mapa.getId(), username)) {
            return;
        }
        throw new IllegalStateException("Sen permiso para acceder a este mapa");
    }
}
