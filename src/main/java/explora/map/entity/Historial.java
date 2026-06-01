package explora.map.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/** Entidade JPA que rexistra cada acción realizada sobre un mapa (crear, editar, eliminar). */
@Entity
@Table(name = "entrada_historial")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Historial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapa_id", nullable = false)
    private Mapa mapa;

    @Column(nullable = false)
    private String usuaria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAccion tipoAccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoElemento tipoElemento;

    private Long elementoId;

    private String elementoNome;

    private String detalle;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataAccion;
}
