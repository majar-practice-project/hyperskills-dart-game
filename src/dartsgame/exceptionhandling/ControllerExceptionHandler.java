package dartsgame.exceptionhandling;

import dartsgame.game.GameConflictException;
import dartsgame.game.GameResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(Map.of("result", Objects.requireNonNull(Objects.requireNonNull(ex.getFieldError()).getDefaultMessage())), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({GameConflictException.class})
    public ResponseEntity<Map<String, String>> handleGameBadRequests(Exception e){
        return new ResponseEntity<>(Map.of("result", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({GameResourceNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleGameNotFoundRequests(Exception e){
        return new ResponseEntity<>(Map.of("result", e.getMessage()), HttpStatus.NOT_FOUND);
    }

}
