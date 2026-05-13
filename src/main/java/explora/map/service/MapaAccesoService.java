package explora.map.service;

import explora.map.entity.Mapa;
import explora.map.entity.TipoMapa;
import explora.map.repository.MapaMembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapaAccesoService {

    private final MapaMembroRepository mapaMembroRepository;

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
