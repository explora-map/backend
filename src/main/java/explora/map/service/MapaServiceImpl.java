package explora.map.service;

import explora.map.dto.MapaColaboracionResponseDTO;
import explora.map.dto.MapaRequestDTO;
import explora.map.dto.MapaResponseDTO;
import explora.map.entity.EstadoConvite;
import explora.map.entity.Mapa;
import explora.map.entity.TipoAccion;
import explora.map.entity.TipoElemento;
import explora.map.entity.TipoMapa;
import explora.map.repository.ConviteRepository;
import explora.map.repository.HistorialRepository;
import explora.map.repository.MapaMembroRepository;
import explora.map.repository.MapaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementación de {@link MapaService} que xestiona o ciclo de vida dos mapas.
 *
 * <p>Responsabilidades principais:</p>
 * <ul>
 *   <li>Creación, edición, eliminación e consulta de mapas.</li>
 *   <li>Control de acceso baseado en propiedade ({@code creadoPor}) e convites aceptados.</li>
 *   <li>Resolución de datos xeográficos (cidade, rexión, país) mediante a API Nominatim.</li>
 *   <li>Rexistro de accións no historial de actividade do mapa.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class MapaServiceImpl implements MapaService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final MapaRepository mapaRepository;
    private final ConviteRepository conviteRepository;
    private final MapaMembroRepository mapaMembroRepository;
    private final HistorialService historialService;
    private final HistorialRepository historialRepository;

    /**
     * Crea un novo mapa e rexistra a acción no historial.
     *
     * <p>Resolve automaticamente os datos xeográficos (cidade, rexión, país)
     * a partir das coordenadas mediante Nominatim. Se a chamada falla,
     * os campos de localización quedan como {@code null}.</p>
     *
     * @param username nome da usuaria creadora do mapa
     * @param dto      datos do novo mapa (nome, coordenadas, tipo, etc.)
     * @return DTO co mapa creado, incluíndo o id asignado pola base de datos
     */
    @Transactional
    @Override
    public MapaResponseDTO novo(String username, MapaRequestDTO dto) {
        Mapa mapa = new Mapa();
        mapa.setNome(dto.getNome());
        mapa.setDescricion(dto.getDescricion());
        mapa.setLatitude(dto.getLatitude());
        mapa.setLonxitude(dto.getLonxitude());
        mapa.setNomeLocalizacion(dto.getNomeLocalizacion());
        mapa.setTipo(dto.getTipo());
        // Set creadoPor explicitly as a safety net for test contexts where
        // the SecurityContext may be empty and AuditorAware returns empty.
        mapa.setCreadoPor(username);
        LocalizacionNominatim loc = resolverLocalizacion(mapa.getLatitude(), mapa.getLonxitude());
        mapa.setCidade(loc.cidade());
        mapa.setRexion(loc.rexion());
        mapa.setPais(loc.pais());
        mapa.setCodigoPais(loc.codigoPais());
        Mapa gardado = mapaRepository.save(mapa);
        historialService.rexistrar(
                gardado,
                username,
                TipoAccion.CREAR,
                TipoElemento.MAPA,
                gardado.getId(),
                gardado.getNome(),
                null
        );
        return toDTO(gardado);
    }

    /**
     * Obtén un mapa polo seu identificador, verificando que a usuaria ten acceso.
     *
     * <p>O acceso a un mapa privado concédese se a usuaria é a creadora ou
     * ten un convite en estado {@code ACEPTADO} para ese mapa.</p>
     *
     * @param mapaId   identificador do mapa a consultar
     * @param username nome da usuaria que solicita o acceso
     * @return DTO co mapa solicitado
     * @throws IllegalArgumentException se non existe ningún mapa co id indicado
     * @throws IllegalStateException    se o mapa é privado e a usuaria non ten acceso
     */
    @Transactional(readOnly = true)
    @Override
    public MapaResponseDTO obterPorId(Long mapaId, String username) {
        Mapa mapa = mapaRepository.findById(mapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + mapaId));
        if (mapa.getTipo() == TipoMapa.PUBLICO) {
            return toDTO(mapa);
        }
        if (mapa.getCreadoPor().equals(username)) {
            return toDTO(mapa);
        }
        boolean tenConviteAceptado = conviteRepository.findByMapaId(mapaId).stream()
                .anyMatch(c -> c.getEstado() == EstadoConvite.ACEPTADO
                        && c.getConvidada().getUsername().equals(username));
        if (!tenConviteAceptado) {
            throw new IllegalStateException("Sen permiso para ver este mapa");
        }
        return toDTO(mapa);
    }

    /**
     * Obtén todos os mapas creados pola usuaria indicada.
     *
     * @param username nome da usuaria propietaria dos mapas
     * @return lista de DTOs cos mapas da usuaria; lista baleira se non ten ningún
     */
    @Override
    public List<MapaResponseDTO> obterPorUsername(String username) {
        return mapaRepository.findByCreadoPor(username)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Edita os datos dun mapa existente.
     *
     * <p>Se as coordenadas cambian respecto ás orixinais, resolve de novo
     * os datos xeográficos mediante Nominatim. Se a chamada falla,
     * os campos de localización quedan como {@code null}.</p>
     *
     * @param id       identificador do mapa a editar
     * @param username nome da usuaria que realiza a edición
     * @param dto      novos datos do mapa
     * @return DTO co mapa actualizado
     * @throws IllegalArgumentException se non existe ningún mapa co id indicado
     * @throws IllegalStateException    se a usuaria non é a creadora do mapa
     */
    @Transactional
    @Override
    public MapaResponseDTO editar(Long id, String username, MapaRequestDTO dto) {
        Mapa mapaExistente = mapaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + id));
        if (!mapaExistente.getCreadoPor().equals(username)) {
            throw new IllegalStateException("Sen permiso para editar este mapa");
        }
        mapaExistente.setNome(dto.getNome());
        mapaExistente.setDescricion(dto.getDescricion());
        mapaExistente.setNomeLocalizacion(dto.getNomeLocalizacion());
        mapaExistente.setTipo(dto.getTipo());
        if (!Objects.equals(mapaExistente.getLatitude(), dto.getLatitude()) ||
                !Objects.equals(mapaExistente.getLonxitude(), dto.getLonxitude())) {
            mapaExistente.setLatitude(dto.getLatitude());
            mapaExistente.setLonxitude(dto.getLonxitude());
            LocalizacionNominatim loc = resolverLocalizacion(dto.getLatitude(), dto.getLonxitude());
            mapaExistente.setCidade(loc.cidade());
            mapaExistente.setRexion(loc.rexion());
            mapaExistente.setPais(loc.pais());
            mapaExistente.setCodigoPais(loc.codigoPais());
        } else {
            mapaExistente.setLatitude(dto.getLatitude());
            mapaExistente.setLonxitude(dto.getLonxitude());
        }
        Mapa editado = mapaRepository.save(mapaExistente);
        historialService.rexistrar(
                editado,
                username,
                TipoAccion.EDITAR,
                TipoElemento.MAPA,
                editado.getId(),
                editado.getNome(),
                null
        );
        return toDTO(editado);
    }

    /**
     * Elimina un mapa e todo o seu historial de actividade asociado.
     *
     * <p>Os marcadores, categorías e membros do mapa elimínanse en cascada
     * a nivel de base de datos mediante {@code @OnDelete(CASCADE)}.</p>
     *
     * @param id       identificador do mapa a eliminar
     * @param username nome da usuaria que solicita a eliminación
     * @throws IllegalArgumentException se non existe ningún mapa co id indicado
     * @throws IllegalStateException    se a usuaria non é a creadora do mapa
     */
    @Transactional
    @Override
    public void eliminar(Long id, String username) {
        Mapa mapa = mapaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + id));
        if (!mapa.getCreadoPor().equals(username)) {
            throw new IllegalStateException("Sen permiso para eliminar este mapa");
        }
        historialRepository.deleteByMapaId(id);
        mapaRepository.delete(mapa);
    }

    /**
     * Obtén os mapas públicos situados dentro dun radio xeográfico dado.
     *
     * <p>A distancia calcúlase coa fórmula de Haversine sobre a esfera terrestre
     * (radio medio: {@value #EARTH_RADIUS_KM} km).</p>
     *
     * @param latitude  latitude do punto central da busca, en graos decimais
     * @param lonxitude lonxitude do punto central da busca, en graos decimais
     * @param radio     radio máximo de busca en quilómetros
     * @return lista de DTOs cos mapas públicos dentro do radio; lista baleira se non hai ningún
     */
    @Override
    public List<MapaResponseDTO> obterPorTipoPublico(Double latitude, Double lonxitude, Double radio) {
        return mapaRepository.findByTipo(TipoMapa.PUBLICO)
                .stream()
                .filter(m -> haversine(latitude, lonxitude, m.getLatitude(), m.getLonxitude()) <= radio)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * Cambia a visibilidade dun mapa entre {@code PUBLICO} e {@code PRIVADO}.
     *
     * <p>Rexistra a acción como {@code EDITAR} no historial do mapa.</p>
     *
     * @param id       identificador do mapa a modificar
     * @param username nome da usuaria que solicita o cambio
     * @param tipo     novo tipo de visibilidade ({@code PUBLICO} ou {@code PRIVADO})
     * @return DTO co mapa actualizado co novo tipo de visibilidade
     * @throws IllegalArgumentException se non existe ningún mapa co id indicado
     * @throws IllegalStateException    se a usuaria non é a creadora do mapa
     */
    @Transactional
    @Override
    public MapaResponseDTO cambiarVisibilidade(Long id, String username, TipoMapa tipo) {
        Mapa mapa = mapaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mapa non atopado: " + id));
        if (!mapa.getCreadoPor().equals(username)) {
            throw new IllegalStateException("Sen permiso para cambiar a visibilidade deste mapa");
        }
        mapa.setTipo(tipo);
        Mapa gardado = mapaRepository.save(mapa);
        historialService.rexistrar(
                gardado,
                username,
                TipoAccion.EDITAR,
                TipoElemento.MAPA,
                gardado.getId(),
                gardado.getNome(),
                null
        );
        return toDTO(gardado);
    }

    /**
     * Obtén todos os mapas nos que a usuaria participa como membro (non como creadora).
     *
     * <p>Inclúe o rol da usuaria en cada mapa para que o frontend poida
     * mostrar as accións dispoñibles segundo os seus permisos.</p>
     *
     * @param username nome da usuaria cuxa participación se consulta
     * @return lista de DTOs de colaboración cos mapas e o rol da usuaria en cada un;
     *         lista baleira se a usuaria non é membro de ningún mapa
     */
    @Transactional(readOnly = true)
    @Override
    public List<MapaColaboracionResponseDTO> obterMapasColaboradora(String username) {
        return mapaMembroRepository.findAllByUsuariaUsername(username)
                .stream()
                .map(m -> {
                    Mapa mapa = m.getMapa();
                    MapaColaboracionResponseDTO dto = new MapaColaboracionResponseDTO(
                            mapa.getId(),
                            mapa.getNome(),
                            mapa.getDescricion(),
                            mapa.getLatitude(),
                            mapa.getLonxitude(),
                            mapa.getNomeLocalizacion(),
                            mapa.getTipo(),
                            mapa.getCreadoPor(),
                            mapa.getDataCreacion(),
                            mapa.getCidade(),
                            mapa.getRexion(),
                            mapa.getPais(),
                            mapa.getCodigoPais(),
                            m.getRol().name()
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private record LocalizacionNominatim(String cidade, String rexion, String pais, String codigoPais) {}

    private LocalizacionNominatim resolverLocalizacion(double latitude, double lonxitude) {
        try {
            String url = String.format(
                    "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=%s&lon=%s&accept-language=gl",
                    latitude, lonxitude
            ).replace(",", ".");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", "ExploraMap/1.0")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            String cidade = extraerCampoNominatim(body, new String[]{"\"city\"", "\"town\"", "\"village\"", "\"municipality\""});
            String rexion = extraerValorJson(body, "\"state\"");
            String pais = extraerValorJson(body, "\"country\"");
            String codigoPais = extraerValorJson(body, "\"country_code\"");

            if (codigoPais != null) codigoPais = codigoPais.toUpperCase();

            return new LocalizacionNominatim(cidade, rexion, pais, codigoPais);

        } catch (Exception e) {
            return new LocalizacionNominatim(null, null, null, null);
        }
    }

    // Extracts the first non-null value from a list of candidate keys within the "address" block
    private String extraerCampoNominatim(String json, String[] chaves) {
        for (String chave : chaves) {
            String valor = extraerValorJson(json, chave);
            if (valor != null && !valor.isBlank()) return valor;
        }
        return null;
    }

    // Extracts a JSON string value by key name (simple string parsing, no library needed)
    private String extraerValorJson(String json, String chave) {
        try {
            int idx = json.indexOf(chave);
            if (idx == -1) return null;
            int colon = json.indexOf(":", idx);
            if (colon == -1) return null;
            int start = json.indexOf("\"", colon + 1);
            if (start == -1) return null;
            int end = json.indexOf("\"", start + 1);
            if (end == -1) return null;
            String valor = json.substring(start + 1, end);
            return valor.isBlank() ? null : valor;
        } catch (Exception e) {
            return null;
        }
    }

    private MapaResponseDTO toDTO(Mapa m) {
        return new MapaResponseDTO(
                m.getId(),
                m.getNome(),
                m.getDescricion(),
                m.getLatitude(),
                m.getLonxitude(),
                m.getNomeLocalizacion(),
                m.getTipo(),
                m.getCreadoPor(),
                m.getDataCreacion(),
                m.getCidade(),
                m.getRexion(),
                m.getPais(),
                m.getCodigoPais()
        );
    }
}
