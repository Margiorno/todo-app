package com.pm.todoapp.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // TODO
    @ExceptionHandler({
            ConversationNotFoundException.class,
            StorageFileNotFoundException.class,
            NotificationNotFoundException.class,
            TaskNotFoundException.class,
            TeamNotFoundException.class
    })
    public ResponseEntity<Map<String,String>> handleNotFoundStatusExceptions(ConversationNotFoundException ex) {
        String message = ex.getMessage();
        Map<String,String> map = new HashMap<>();
        map.put("message", message);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
    }

    @ExceptionHandler({
            UserNotConversationParticipantException.class,
            TaskAccessDeniedException.class,
            InvalidTeamInviteException.class
    })
    public ResponseEntity<Map<String,String>> handleForbiddenStatusExceptions(UserNotConversationParticipantException ex) {
        String message = ex.getMessage();
        Map<String,String> map = new HashMap<>();
        map.put("message", message);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<Map<String,String>> handleStorageException(StorageException ex) {
        String message = ex.getMessage();
        Map<String,String> map = new HashMap<>();
        map.put("message", message);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Validation failed");

        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    Map<String, String> err = new HashMap<>();
                    err.put("field", error.getField());
                    err.put("error", error.getDefaultMessage());
                    return err;
                })
                .toList();

        body.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

}