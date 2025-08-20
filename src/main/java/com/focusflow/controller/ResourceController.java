package com.focusflow.controller;

import com.focusflow.model.Project;
import com.focusflow.model.Resource;
import com.focusflow.model.User;
import com.focusflow.repository.ProjectRepository;
import com.focusflow.repository.ResourceRepository;
import com.focusflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/resources")
public class ResourceController {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping
    public String resourcesPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Resource> resources = resourceRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<Project> projects = projectRepository.findAllByUser(user);
        
        // Filter out resources without projects for display
        List<Resource> validResources = resources.stream()
            .filter(resource -> resource.getProject() != null)
            .toList();
        
        model.addAttribute("resources", validResources);
        model.addAttribute("projects", projects);
        model.addAttribute("user", user);
        return "resources";
    }

    @GetMapping("/project/{projectId}")
    public String projectResources(@AuthenticationPrincipal UserDetails userDetails, 
                                 @PathVariable Long projectId, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Resource> resources = resourceRepository.findByUserIdAndProjectIdOrderByCreatedAtDesc(user.getId(), projectId);
        Optional<Project> project = projectRepository.findById(projectId);
        List<Project> projects = projectRepository.findAllByUser(user);
        
        // Filter out resources without projects for display
        List<Resource> validResources = resources.stream()
            .filter(resource -> resource.getProject() != null)
            .toList();
        
        model.addAttribute("resources", validResources);
        model.addAttribute("currentProject", project.orElse(null));
        model.addAttribute("projects", projects);
        model.addAttribute("user", user);
        return "resources";
    }

    @PostMapping("/add")
    public String addResource(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam("title") String title,
                            @RequestParam("description") String description,
                            @RequestParam("projectId") Long projectId,
                            @RequestParam("resourceType") String resourceType,
                            @RequestParam(value = "url", required = false) String url,
                            @RequestParam(value = "file", required = false) MultipartFile file,
                            RedirectAttributes redirectAttributes) {
        
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/resources";
        }
        
        Optional<Project> project = projectRepository.findById(projectId);
        
        if (!project.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Project not found");
            return "redirect:/resources";
        }

        Resource resource = new Resource();
        resource.setTitle(title);
        resource.setDescription(description);
        resource.setUser(user);
        resource.setProject(project.get());

        if ("LINK".equals(resourceType)) {
            resource.setUrl(normalizeUrl(url));
            resource.setFileType("LINK");
        } else if (file != null && !file.isEmpty()) {
            try {
                // Check file size (50MB limit)
                if (file.getSize() > 50 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "File size too large. Maximum size is 50MB.");
                    return "redirect:/resources";
                }

                // Create upload directory if it doesn't exist
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generate unique filename
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Invalid filename");
                    return "redirect:/resources";
                }
                
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                
                // Save file
                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(file.getInputStream(), filePath);

                // Set resource properties
                resource.setFileName(uniqueFilename);
                resource.setFileType(getFileType(fileExtension));
                resource.setFileSize(formatFileSize(file.getSize()));
                resource.setUrl("/uploads/" + uniqueFilename);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
                return "redirect:/resources";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Unexpected error during file upload: " + e.getMessage());
                return "redirect:/resources";
            }
        }

        resourceRepository.save(resource);
        redirectAttributes.addFlashAttribute("success", "Resource added successfully");
        return "redirect:/resources";
    }

    @PostMapping("/edit/{id}")
    public String editResource(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable Long id,
                             @RequestParam("title") String title,
                             @RequestParam("description") String description,
                             @RequestParam("projectId") Long projectId,
                             @RequestParam(value = "url", required = false) String url,
                             RedirectAttributes redirectAttributes) {
        
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/resources";
        }
        
        Optional<Resource> resourceOpt = resourceRepository.findById(id);
        
        if (!resourceOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Resource not found");
            return "redirect:/resources";
        }

        Resource resource = resourceOpt.get();
        if (!resource.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/resources";
        }

        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Project not found");
            return "redirect:/resources";
        }

        resource.setTitle(title);
        resource.setDescription(description);
        resource.setProject(project.get());
        
        if ("LINK".equals(resource.getFileType())) {
            resource.setUrl(normalizeUrl(url));
        }

        resourceRepository.save(resource);
        redirectAttributes.addFlashAttribute("success", "Resource updated successfully");
        return "redirect:/resources";
    }

    private String normalizeUrl(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String trimmed = input.trim();
        if (!(trimmed.startsWith("http://") || trimmed.startsWith("https://"))) {
            return "https://" + trimmed;
        }
        return trimmed;
    }

    @PostMapping("/delete/{id}")
    public String deleteResource(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/resources";
        }
        
        Optional<Resource> resourceOpt = resourceRepository.findById(id);
        
        if (!resourceOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Resource not found");
            return "redirect:/resources";
        }

        Resource resource = resourceOpt.get();
        if (!resource.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/resources";
        }

        // Delete file if it exists
        if (resource.getFileName() != null) {
            try {
                Path filePath = Paths.get(UPLOAD_DIR, resource.getFileName());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log error but continue with resource deletion
                System.err.println("Failed to delete file: " + e.getMessage());
            }
        }

        resourceRepository.delete(resource);
        redirectAttributes.addFlashAttribute("success", "Resource deleted successfully");
        return "redirect:/resources";
    }

    private String getFileType(String extension) {
        String ext = extension.toLowerCase();
        if (ext.equals(".pdf")) return "PDF";
        if (ext.equals(".doc") || ext.equals(".docx")) return "DOC";
        if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".gif")) return "IMAGE";
        return "FILE";
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
