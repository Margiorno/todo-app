package com.pm.todoapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String taskNotFound(TaskNotFoundException e, Model model) {

        model.addAttribute("message", e.getMessage());
        model.addAttribute("link","/task/list");

        return "error/404";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleTypeMismatchException(MethodArgumentTypeMismatchException ex, Model model) {
        String errorMessage;
        Class<?> requiredType = ex.getRequiredType();

        if (requiredType != null && requiredType.equals(UUID.class)) {
            errorMessage = String.format("Task ID is incorrect: '%s'.", ex.getValue());

        } else {
            errorMessage = String.format(
                    "Parameter '%s' type mismatch: '%s'",
                    ex.getName(),
                    ex.getValue()
            );
        }

        model.addAttribute("message", errorMessage);
        model.addAttribute("link", "/task/list");
        return "error/400";
    }

    @ExceptionHandler(InvalidTokenException.class)
    public RedirectView handleInvalidToken(InvalidTokenException ex) {

        return new RedirectView("/auth");
    }

}
