package explora.map.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        indexes = {
                @Index(
                        name = "idx_mapa_tipo_lat_lng",
                        columnList = "tipo, latitude, lonxitude"
                )
        }
)
public class Mapa {
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

    // NOTE(production): ddl-auto=create-drop recreates schema on restart.
    // When migrating to PostgreSQL, generate a proper Flyway/Liquibase migration
    // to ADD COLUMN for cidade, rexion, pais, codigoPais and ALTER COLUMN
    // nomeLocalizacion TYPE VARCHAR(150).
    @Column(nullable = false, length = 150)
    private String nomeLocalizacion;

    @Column
    private String cidade;

    @Column
    private String rexion;

    @Column
    private String pais;

    @Column(length = 2)
    private String codigoPais;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMapa tipo;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCreacion;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime dataModificacion;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String creadoPor; //username
}
