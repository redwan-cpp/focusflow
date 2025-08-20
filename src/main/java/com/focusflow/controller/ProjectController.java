package com.focusflow.controller;

import com.focusflow.model.Project;
import com.focusflow.model.User;
import com.focusflow.repository.ProjectRepository;
import com.focusflow.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectController(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    // List all projects for the logged-in user
    @GetMapping
    public String projectsPage(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        List<Project> projects = projectRepository.findAllByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("projects", projects);
        model.addAttribute("newProject", new Project());
        return "projects";
    }

    // Create a new project
    @PostMapping("/add")
    public String addProject(@ModelAttribute("newProject") Project project, Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user == null) {
                return "redirect:/login";
            }

            project.setUser(user);
            
            // Set default status if not provided
            if (project.getStatus() == null || project.getStatus().isEmpty()) {
                project.setStatus("ACTIVE");
            }

            projectRepository.save(project);
            return "redirect:/projects";
        } catch (Exception e) {
            System.err.println("Error adding project: " + e.getMessage());
            return "redirect:/projects?error=Failed+to+add+project";
        }
    }

    // Delete a project
    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Long id, Principal principal) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null && project.getUser().getEmail().equals(principal.getName())) {
            projectRepository.delete(project);
        }
        return "redirect:/projects";
    }

    // Show edit form
    @GetMapping("/{id}/edit")
    public String editProjectForm(@PathVariable Long id, Model model, Principal principal) {
        Project project = projectRepository.findById(id).orElse(null);
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (project == null || !project.getUser().getEmail().equals(principal.getName())) {
            return "redirect:/projects";
        }
        model.addAttribute("user", user);
        model.addAttribute("editProject", project);
        return "edit-project";
    }

    // Handle edit form submission
    @PostMapping("/{id}/edit")
    public String editProject(@PathVariable Long id, @ModelAttribute("editProject") Project updatedProject, Principal principal) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null && project.getUser().getEmail().equals(principal.getName())) {
            project.setName(updatedProject.getName());
            project.setDescription(updatedProject.getDescription());
            project.setDeadline(updatedProject.getDeadline());
            project.setStatus(updatedProject.getStatus());
            projectRepository.save(project);
        }
        return "redirect:/projects";
    }

    // Change project status
    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam String status, Principal principal) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null && project.getUser().getEmail().equals(principal.getName())) {
            project.setStatus(status);
            projectRepository.save(project);
        }
        return "redirect:/projects";
    }
} 