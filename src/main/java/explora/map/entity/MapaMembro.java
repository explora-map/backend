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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/** Entidade JPA que asocia unha usuaria cun mapa privado, co seu rol de acceso. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "mapa_membro",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"mapa_id", "usuaria_id"})
        }
)
public class MapaMembro {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapa_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Mapa mapa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuaria_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuaria usuaria;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RolMapa rol;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime dataCreacion;
}
