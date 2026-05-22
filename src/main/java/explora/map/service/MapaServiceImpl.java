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

@Service
@RequiredArgsConstructor
public class MapaServiceImpl implements MapaService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final MapaRepository mapaRepository;
    private final ConviteRepository conviteRepository;
    private final MapaMembroRepository mapaMembroRepository;
    private final HistorialService historialService;
    private final HistorialRepository historialRepository;

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

    @Override
    public List<MapaResponseDTO> obterPorUsername(String username) {
        return mapaRepository.findByCreadoPor(username)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

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
