package explora.map.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    @Id @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuaria_id", nullable = false)
    private Usuaria usuaria;

    @Column(nullable = false)
    private LocalDateTime dataExpiracion;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime dataCreacion;

    @Column(nullable = false)
    private Boolean isRevoked;

    @PrePersist
    protected void onCreate() {
        this.dataCreacion = LocalDateTime.now();
    }

}
