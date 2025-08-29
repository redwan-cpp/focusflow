// src/main/java/com/focusflow/controller/AuthController.java
package com.focusflow.controller;

import com.focusflow.model.User;
import com.focusflow.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

@Controller
public class AuthController {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final String uploadDir = "uploads/";

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

    // Dashboard is handled by DashboardController

    // Stats page is handled by StatsController

    @GetMapping("/profile")
    public String profilePage(Model model, Principal principal) {
        User user = repo.findByEmail(principal.getName()).orElse(null);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("name") String name,
                               @RequestParam("email") String email,
                               @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = repo.findByEmail(principal.getName()).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/profile";
            }

            // Check if email is being changed and if it's already taken
            if (!user.getEmail().equals(email)) {
                if (repo.findByEmail(email).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Email is already taken");
                    return "redirect:/profile";
                }
            }

            user.setName(name);
            user.setEmail(email);

            // Handle profile picture upload
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String fileName = saveProfilePicture(profilePicture);
                user.setAvatarUrl("/uploads/" + fileName);
            }

            repo.save(user);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = repo.findByEmail(principal.getName()).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/profile";
            }

            // Verify current password
            if (!encoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                return "redirect:/profile";
            }

            // Check if new passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "New passwords do not match");
                return "redirect:/profile";
            }

            // Validate new password (minimum 6 characters)
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "New password must be at least 6 characters long");
                return "redirect:/profile";
            }

            // Update password
            user.setPassword(encoder.encode(newPassword));
            repo.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to change password: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    private String saveProfilePicture(MultipartFile file) throws IOException {
        // Create uploads directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        return fileName;
    }
}
