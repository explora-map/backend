package explora.map.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_verificacion")
@Data
public class TokenVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuaria_id", nullable = false)
    private Usuaria usuaria;

    @Column(nullable = false)
    private LocalDateTime dataExpiracion;

    @Column(nullable = false)
    private boolean usado = false;
}
