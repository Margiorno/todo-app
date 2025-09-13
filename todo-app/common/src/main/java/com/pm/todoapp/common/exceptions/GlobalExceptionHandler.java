package com.pm.todoapp.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // TODO
    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleConversationNotFoundException(ConversationNotFoundException ex) {
        String message = ex.getMessage();
        Map<String,String> map = new HashMap<>();
        map.put("message", message);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
    }

    @ExceptionHandler(UserNotConversationParticipantException.class)
    public ResponseEntity<Map<String,String>> handleUserNotConversationParticipantException(UserNotConversationParticipantException ex) {
        String message = ex.getMessage();
        Map<String,String> map = new HashMap<>();
        map.put("message", message);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
    }

}