package com.pm.todoapp.controller;

import com.pm.todoapp.dto.LoginRequestDTO;
import com.pm.todoapp.dto.RegisterRequestDTO;
import com.pm.todoapp.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


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
        String userId = authService.registerUser(registerRequest);

        Cookie userCookie = new Cookie("userCookie", userId);
        userCookie.setPath("/");
        response.addCookie(userCookie);

        redirectAttributes.addFlashAttribute("successMessage", "Register success");

        return "redirect:/";
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute LoginRequestDTO loginRequestDTO, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        String userId = authService.loginUser(loginRequestDTO);

        Cookie userCookie = new Cookie("userCookie", userId);
        userCookie.setPath("/");
        response.addCookie(userCookie);

        redirectAttributes.addFlashAttribute("successMessage", "Login success");
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String handleLogout(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Cookie userCookie = new Cookie("userCookie", null);
        userCookie.setPath("/");
        userCookie.setMaxAge(0);
        response.addCookie(userCookie);

        return "redirect:/auth";
    }

    @GetMapping("/unauthorized")
    public String handleUnauthorized(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Your session has expired or token is invalid. Login required.");

        return "redirect:/auth";
    }


}
