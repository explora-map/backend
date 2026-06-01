package explora.map.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/** Entidade JPA que representa un punto de interese xeográfico dentro dun mapa. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "marcador")
public class Marcador {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricion;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double lonxitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapa_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Mapa mapa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", foreignKey = @ForeignKey(name = "fk_marcador_categoria"), nullable = true)
    private Categoria categoria;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String creadoPor;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCreacion;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime dataModificacion;
}
