package explora.map.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/** Entidade JPA que representa unha usuaria rexistrada na aplicación. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "correo")
        }
)
public class Usuaria {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private String hashPassword;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RolApp rol;

    @Column(nullable = false)
    @Builder.Default
    private String idioma = "gl";

    @Column(nullable = false)
    @Builder.Default
    private boolean verificada = false;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime dataCreacion;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime dataModificacion;

    @PrePersist
    protected void onCreate() {
        this.dataCreacion = LocalDateTime.now();
    }
}
