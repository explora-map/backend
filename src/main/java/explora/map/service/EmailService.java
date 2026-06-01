package explora.map.service;

/** Interface do servizo de envío de correo electrónico. */
public interface EmailService {
    void enviarCorreoVerificacion(String destinatario, String nome, String token);
}
