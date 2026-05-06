package explora.map.service;

import explora.map.entity.EstadoConvite;
import explora.map.entity.Mapa;
import explora.map.entity.TipoMapa;
import explora.map.repository.ConviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapaAccesoService {

    private final ConviteRepository conviteRepository;

    public void verificar(Mapa mapa, String username) {
        if (mapa.getTipo() == TipoMapa.PUBLICO) {
            return;
        }
        if (mapa.getCreadoPor().equals(username)) {
            return;
        }
        boolean tenConviteAceptado = conviteRepository.findByMapaId(mapa.getId()).stream()
                .anyMatch(c -> c.getEstado() == EstadoConvite.ACEPTADO
                        && c.getConvidada().getUsername().equals(username));
        if (!tenConviteAceptado) {
            throw new IllegalStateException("Sen permiso para acceder a este mapa");
        }
    }
}
