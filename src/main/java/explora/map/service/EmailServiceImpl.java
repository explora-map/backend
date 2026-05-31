package explora.map.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void enviarCorreoVerificacion(String destinatario, String nome, String token) {
        SimpleMailMessage mensaxe = new SimpleMailMessage();
        mensaxe.setTo(destinatario);
        mensaxe.setSubject("Verifica a túa conta en Explora");
        mensaxe.setText(
            "Ola, " + nome + "!\n\n" +
            "Grazas por rexistrarte en Explora. Para activar a túa conta, preme na seguinte ligazón:\n\n" +
            baseUrl + "/verificar?token=" + token + "\n\n" +
            "Esta ligazón caduca en 24 horas.\n\n" +
            "Se non creaches esta conta, ignora este correo.\n\n" +
            "O equipo de Explora"
        );
        mensaxe.setFrom("noreply@explora-mapa.eu");
        mailSender.send(mensaxe);
    }
}
