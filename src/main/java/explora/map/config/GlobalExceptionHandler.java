package explora.map.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Erros de validación Bean Validation (@Valid).
     * Devolve 400 con mapa { campo → mensaxe }.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Erros de lóxica de negocio (username/correo duplicado, etc.)
     * Devolve 400 con { "message": "..." }
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException ex) {
        log.warn("Erro de negocio: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Credenciais incorrectas no login.
     * Devolve 401 con { "message": "..." }
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(
            BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Username ou contrasinal incorrecto."));
    }

    /**
     * Erros de refresh token (expirado, non encontrado) e outros RuntimeException
     * relacionados co fluxo de autenticación. Devolve 401.
     * NOTA: usa RuntimeException porque aínda non hai tipos de excepción propios;
     * cando se engadan (ex. TokenException), afinar este handler.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(
            RuntimeException ex) {
        log.warn("Erro de autenticación/token: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", ex.getMessage()));
    }
}
