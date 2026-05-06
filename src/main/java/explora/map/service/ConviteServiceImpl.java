package explora.map.service;

import explora.map.dto.ConviteRequestDTO;
import explora.map.dto.ConviteResponseDTO;
import explora.map.entity.Convite;
import explora.map.entity.EstadoConvite;
import explora.map.entity.Mapa;
import explora.map.entity.Usuaria;
import explora.map.repository.ConviteRepository;
import explora.map.repository.MapaRepository;
import explora.map.repository.UsuariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConviteServiceImpl implements ConviteService {

    private final ConviteRepository conviteRepository;
    private final UsuariaRepository usuariaRepository;
    private final MapaRepository mapaRepository;

    @Transactional
    @Override
    public ConviteResponseDTO novo(String usernameAnfitrioa, ConviteRequestDTO dto) {
        Usuaria anfitrioa = usuariaRepository.findByUsername(usernameAnfitrioa)
                .orElseThrow(() -> new UsernameNotFoundException(usernameAnfitrioa));
        Usuaria convidada = usuariaRepository.findByUsername(dto.getUsernameConvidada())
                .orElseThrow(() -> new UsernameNotFoundException(dto.getUsernameConvidada()));

        if (anfitrioa.getId().equals(convidada.getId())) {
            throw new IllegalArgumentException("Non se pode convidar a si mesma");
        }

        Mapa mapa = mapaRepository.findById(dto.getMapaId())
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + dto.getMapaId()));

        if (!mapa.getCreadoPor().equals(usernameAnfitrioa)) {
            throw new IllegalStateException("Non podes convidar a un mapa que non che pertence");
        }

        Convite convite = new Convite();
        convite.setAnfitrioa(anfitrioa);
        convite.setConvidada(convidada);
        convite.setMapa(mapa);
        convite.setToken(UUID.randomUUID());
        convite.setEstado(EstadoConvite.PENDENTE);
        convite.setDataExpiracion(LocalDateTime.now().plusDays(7));

        return toDTO(conviteRepository.save(convite));
    }

    @Override
    public List<ConviteResponseDTO> obterPorUsername(String username) {
        Usuaria anfitrioa = usuariaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return conviteRepository.findByAnfitrioa(anfitrioa)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ConviteResponseDTO> obterRecibidos(String username) {
        Usuaria convidada = usuariaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return conviteRepository.findByConvidada(convidada)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void aceptar(UUID token, String username) {
        Convite convite = findByTokenOrThrow(token);
        checkIsPendente(convite);
        if (!convite.getConvidada().getUsername().equals(username)) {
            throw new IllegalStateException("Sen permiso para aceptar este convite");
        }
        convite.setEstado(EstadoConvite.ACEPTADO);
        conviteRepository.save(convite);
    }

    @Transactional
    @Override
    public void rexeitar(UUID token, String username) {
        Convite convite = findByTokenOrThrow(token);
        checkIsPendente(convite);
        if (!convite.getConvidada().getUsername().equals(username)) {
            throw new IllegalStateException("Sen permiso para rexeitar este convite");
        }
        convite.setEstado(EstadoConvite.REXEITADO);
        conviteRepository.save(convite);
    }

    @Transactional
    @Override
    public void cancelar(UUID token, String username) {
        Convite convite = findByTokenOrThrow(token);
        if (!convite.getAnfitrioa().getUsername().equals(username)) {
            throw new IllegalStateException("Sen permiso para cancelar este convite");
        }
        convite.setEstado(EstadoConvite.CANCELADO);
        conviteRepository.save(convite);
    }

    private Convite findByTokenOrThrow(UUID token) {
        Convite convite = conviteRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Convite non atopado: " + token));
        // Auto-expire: if still PENDENTE and past expiration, flip estado and persist.
        if (convite.getEstado() == EstadoConvite.PENDENTE
                && LocalDateTime.now().isAfter(convite.getDataExpiracion())) {
            convite.setEstado(EstadoConvite.EXPIRADO);
            conviteRepository.save(convite);
        }
        return convite;
    }

    /** Throws if the convite is not in PENDENTE state, with a precise message per state. */
    private void checkIsPendente(Convite convite) {
        switch (convite.getEstado()) {
            case EXPIRADO  -> throw new IllegalStateException("Este convite xa expirou");
            case CANCELADO -> throw new IllegalStateException("Este convite foi cancelado pola anfitrioa");
            case ACEPTADO  -> throw new IllegalStateException("Este convite xa foi aceptado");
            case REXEITADO -> throw new IllegalStateException("Este convite xa foi rexeitado");
            default        -> {} // PENDENTE — ok to proceed
        }
    }

    private ConviteResponseDTO toDTO(Convite c) {
        return new ConviteResponseDTO(
                c.getToken(),
                c.getMapa().getId(),
                c.getMapa().getNome(),
                c.getAnfitrioa().getUsername(),
                c.getConvidada().getUsername(),
                c.getEstado(),
                c.getDataExpiracion()
        );
    }
}