package com.pm.todoapp.controller;

import com.pm.todoapp.dto.LoginRequestDTO;
import com.pm.todoapp.dto.RegisterRequestDTO;
import com.pm.todoapp.dto.TaskRequestDTO;
import com.pm.todoapp.model.Priority;
import com.pm.todoapp.service.AuthService;
import com.pm.todoapp.service.UsersService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    //TODO token generation instead of simple id
    @GetMapping("/register")
    public String showRegisterForm(Model model) {

        model.addAttribute("registerRequest", new RegisterRequestDTO());

        return "register-form";
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


    @GetMapping("/login")
    public String showLoginForm(Model model) {

        model.addAttribute("loginRequest", new LoginRequestDTO());

        return "login-form";
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
