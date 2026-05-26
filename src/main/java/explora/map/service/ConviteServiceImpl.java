package explora.map.service;

import explora.map.dto.ConviteRequestDTO;
import explora.map.dto.ConviteResponseDTO;
import explora.map.entity.Convite;
import explora.map.entity.EstadoConvite;
import explora.map.entity.MapaMembro;
import explora.map.entity.Mapa;
import explora.map.entity.RolMapa;
import explora.map.entity.TipoMapa;
import explora.map.entity.Usuaria;
import explora.map.repository.ConviteRepository;
import explora.map.repository.MapaMembroRepository;
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

/**
 * Implementación do servizo de xestión do ciclo de vida dos convites.
 *
 * <p>Xestiona o envío e a resolución de convites para acceder a mapas privados.
 * Os convites pasan polos seguintes estados:</p>
 * <ul>
 *   <li>{@code PENDENTE}: convite enviado, agardando resposta.</li>
 *   <li>{@code ACEPTADO}: a convidada aceptou; créase automaticamente a entrada en {@code MapaMembro}.</li>
 *   <li>{@code REXEITADO}: a convidada rexeitou o convite.</li>
 *   <li>{@code CANCELADO}: a anfitrioa cancelou o convite antes de ser resolto.</li>
 *   <li>{@code EXPIRADO}: o convite caducou; márcase de forma lazy ao intentar usalo.</li>
 * </ul>
 * <p>Só a propietaria do mapa pode enviar convites.</p>
 */
@Service
@RequiredArgsConstructor
public class ConviteServiceImpl implements ConviteService {

    private final ConviteRepository conviteRepository;
    private final UsuariaRepository usuariaRepository;
    private final MapaRepository mapaRepository;
    private final MapaMembroRepository mapaMembroRepository;

    /**
     * Crea e persiste un novo convite a un mapa privado.
     *
     * <p>Só a propietaria do mapa pode enviar convites. O rol asignado á convidada
     * pódese indicar explicitamente no DTO; se non, asígnase {@code MEMBRO} para mapas
     * privados ou {@code COLABORADORA} para o resto. O convite expira aos 7 días.</p>
     *
     * @param usernameAnfitrioa username da usuaria que envía o convite (debe ser propietaria do mapa)
     * @param dto               datos do convite: username da convidada, id do mapa e rol opcional
     * @return DTO co convite creado e o seu token UUID
     * @throws IllegalArgumentException se a anfitrioa, a convidada ou o mapa non existen,
     *                                  ou se se intenta convidar a si mesma
     * @throws IllegalStateException    se a anfitrioa non é propietaria do mapa
     */
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

        RolMapa rolFinal = dto.getRol() != null
                ? dto.getRol()
                : (mapa.getTipo() == TipoMapa.PRIVADO ? RolMapa.MEMBRO : RolMapa.COLABORADORA);

        Convite convite = new Convite();
        convite.setAnfitrioa(anfitrioa);
        convite.setConvidada(convidada);
        convite.setMapa(mapa);
        convite.setToken(UUID.randomUUID());
        convite.setEstado(EstadoConvite.PENDENTE);
        convite.setRol(rolFinal);
        convite.setDataExpiracion(LocalDateTime.now().plusDays(7));

        return toDTO(conviteRepository.save(convite));
    }

    /**
     * Devolve os convites enviados pola usuaria.
     *
     * @param username username da usuaria anfitrioa
     * @return lista de convites enviados, en calquera estado
     */
    @Override
    public List<ConviteResponseDTO> obterPorUsername(String username) {
        Usuaria anfitrioa = usuariaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return conviteRepository.findByAnfitrioa(anfitrioa)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Devolve os convites recibidos pola usuaria.
     *
     * @param username username da usuaria convidada
     * @return lista de convites recibidos, en calquera estado
     */
    @Override
    public List<ConviteResponseDTO> obterRecibidos(String username) {
        Usuaria convidada = usuariaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return conviteRepository.findByConvidada(convidada)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Acepta un convite pendente e incorpora a convidada ao mapa.
     *
     * <p>Cambia o estado do convite a {@code ACEPTADO} e crea a entrada en
     * {@code MapaMembro} co rol definido no convite, se aínda non existe.
     * Se o convite está caducado, márcase como {@code EXPIRADO} de forma lazy.</p>
     *
     * @param token    token UUID que identifica o convite
     * @param username username da usuaria que acepta (debe ser a convidada)
     * @throws IllegalArgumentException se o convite non existe
     * @throws IllegalStateException    se o convite non está en estado PENDENTE,
     *                                  ou se a usuaria non é a destinataria do convite
     */
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

        if (!mapaMembroRepository.existsByMapaIdAndUsuariaUsername(
                convite.getMapa().getId(), convite.getConvidada().getUsername())) {
            MapaMembro membro = MapaMembro.builder()
                    .mapa(convite.getMapa())
                    .usuaria(convite.getConvidada())
                    .rol(convite.getRol())
                    .build();
            mapaMembroRepository.save(membro);
        }
    }

    /**
     * Rexeita un convite pendente.
     *
     * <p>Cambia o estado do convite a {@code REXEITADO}.
     * Se o convite está caducado, márcase como {@code EXPIRADO} de forma lazy.</p>
     *
     * @param token    token UUID que identifica o convite
     * @param username username da usuaria que rexeita (debe ser a convidada)
     * @throws IllegalArgumentException se o convite non existe
     * @throws IllegalStateException    se o convite non está en estado PENDENTE,
     *                                  ou se a usuaria non é a destinataria do convite
     */
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

    /**
     * Cancela un convite, impedindo que sexa resolto pola convidada.
     *
     * <p>Cambia o estado do convite a {@code CANCELADO}.
     * Pode aplicarse a convites en calquera estado.</p>
     *
     * @param token    token UUID que identifica o convite
     * @param username username da usuaria que cancela (debe ser a anfitrioa)
     * @throws IllegalArgumentException se o convite non existe
     * @throws IllegalStateException    se a usuaria non é a anfitrioa do convite
     */
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