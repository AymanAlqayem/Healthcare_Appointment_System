package org.example.healthcare_appointment_system.config;

import org.example.healthcare_appointment_system.dto.ErrorResponse;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

/**
 * @RestControllerAdvice = combination of:
 *       @ControllerAdvice → makes this class a global error handler across all controllers.
 *       @ResponseBody → ensures the response is JSON (like a normal @RestController).
 *       This means you don’t need try/catch everywhere in your controllers
 *       errors bubble up here automatically.
 * */
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Triggered when @Valid DTO validation fails (e.g., @NotBlank, @Email).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        String msg = ex.getBindingResult().getAllErrors().stream()
                .map(e -> e.getDefaultMessage()).findFirst().orElse("Validation error");
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    //Catches any IllegalArgumentException in your code
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
    }

    /**
     * Returns an ErrorResponse object with:
     *  timestamp
     *  status (HTTP code)
     *  message (error details)
     *  path (the request URL)
     * */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest req) {
        var path = req.getDescription(false).replace("uri=", "");
        return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(), message, path));
    }
}