package com.pm.todoapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            TeamNotFoundException.class,
            TaskNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundExceptions(RuntimeException e, Model model) {

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

    @ExceptionHandler({
            TeamRequiredException.class,
            TaskAccessDeniedException.class
    })
    public String handleBadRequestExceptions(RuntimeException ex, Model model) {

        model.addAttribute("message", ex.getMessage());
        model.addAttribute("link", "/task/list");

        return "error/400";
    }

    @ExceptionHandler({
            UnauthorizedException.class,
            UserNotFoundException.class,
            UserRequiredException.class
    })
    public String handleUnauthorizedExceptions(RuntimeException ex, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        return "redirect:/auth?form=login";
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public String handleRegistrationExceptions(RuntimeException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        return "redirect:/auth?form=register";
    }
}
