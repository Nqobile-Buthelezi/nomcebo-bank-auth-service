package za.co.nomcebo.bank.auth.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import za.co.nomcebo.bank.auth.dto.ErrorResponseDTO;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler( UserAlreadyExistsException.class )
    public ResponseEntity<ErrorResponseDTO> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex
    )
    {
        log.warn(
                "User already exists {}",
                ex.getMessage()
        );

        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setMessage( "User already exists." );

        return ResponseEntity.badRequest().body( errorResponse );
    }
}
