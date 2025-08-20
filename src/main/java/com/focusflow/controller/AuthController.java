// src/main/java/com/focusflow/controller/AuthController.java
package com.focusflow.controller;

import com.focusflow.model.User;
import com.focusflow.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.security.Principal;

@Controller
public class AuthController {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public AuthController(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @GetMapping("/register")
    public String registerPage(Model model, @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("user", new User());
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@ModelAttribute("user") User user, Model model) {
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            // Redirect with error message if email exists
            return "redirect:/register?error=Email+already+registered";
        }
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Model model, Principal principal) {
        User user = repo.findByEmail(principal.getName()).orElse(null);
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/resources")
    public String resourcesPage(Model model, Principal principal) {
        User user = repo.findByEmail(principal.getName()).orElse(null);
        model.addAttribute("user", user);
        return "resources";
    }

    @GetMapping("/stats")
    public String statsPage(Model model, Principal principal) {
        User user = repo.findByEmail(principal.getName()).orElse(null);
        model.addAttribute("user", user);
        return "stats";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, Principal principal) {
        User user = repo.findByEmail(principal.getName()).orElse(null);
        model.addAttribute("user", user);
        return "profile";
    }
}
