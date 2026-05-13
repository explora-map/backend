package explora.map.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_anfitrioa", columnList = "anfitrioa_id"),
        @Index(name = "idx_convidada", columnList = "convidada_id"),
        @Index(name = "idx_mapa", columnList = "mapa_id")
})
public class Convite {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anfitrioa_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuaria anfitrioa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convidada_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuaria convidada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapa_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Mapa mapa;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID token;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoConvite estado;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RolMapa rol;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime dataCreacion;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime dataModificacion;

    @Column(nullable = false)
    private LocalDateTime dataExpiracion;
}
