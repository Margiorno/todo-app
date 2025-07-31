package com.pm.todoapp.controller;

import com.pm.todoapp.dto.LoginRequestDTO;
import com.pm.todoapp.dto.RegisterRequestDTO;
import com.pm.todoapp.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    //TODO token generation instead of simple id
    @GetMapping
    public String showAuthForm(Model model, @RequestParam(value = "form", defaultValue = "login") String form) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequestDTO());
        }
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequestDTO());
        }

        model.addAttribute("form", form);
        return "auth-form";
    }

    @PostMapping("/register")
    public String handleRegister(@ModelAttribute RegisterRequestDTO registerRequest, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        UUID userId = authService.registerUser(registerRequest);

        Cookie userCookie = new Cookie("userCookie", userId.toString());
        userCookie.setPath("/");
        response.addCookie(userCookie);

        redirectAttributes.addFlashAttribute("successMessage", "Register success");

        return "redirect:/";
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute LoginRequestDTO loginRequestDTO, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        UUID userId = authService.loginUser(loginRequestDTO);

        Cookie userCookie = new Cookie("userCookie", userId.toString());
        userCookie.setPath("/");
        response.addCookie(userCookie);

        redirectAttributes.addFlashAttribute("successMessage", "Login success");
        return "redirect:/";
    }
}
