package explora.map.service;

public interface EmailService {
    void enviarCorreoVerificacion(String destinatario, String nome, String token);
}
