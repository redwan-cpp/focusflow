package com.focusflow.controller;

import com.focusflow.model.Project;
import com.focusflow.model.Statistic;
import com.focusflow.model.Task;
import com.focusflow.model.User;
import com.focusflow.repository.ProjectRepository;
import com.focusflow.repository.StatisticRepository;
import com.focusflow.repository.TaskRepository;
import com.focusflow.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final StatisticRepository statisticRepository;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository, ProjectRepository projectRepository, StatisticRepository statisticRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.statisticRepository = statisticRepository;
    }

    // List all tasks for the logged-in user
    @GetMapping
    public String tasksPage(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        List<Task> unfinished = taskRepository.findByUserAndCompletedFalseOrderByCreatedAtDesc(user);
        List<Task> finished = taskRepository.findByUserAndCompletedTrueOrderByCompletedAtDesc(user);
        List<Project> projects = projectRepository.findAllByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("unfinished", unfinished);
        model.addAttribute("finished", finished);
        model.addAttribute("projects", projects);
        model.addAttribute("newTask", new Task());
        return "tasks";
    }

    // Create a new task
    @PostMapping("/add")
    public String addTask(@ModelAttribute("newTask") Task task, Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user == null) {
                return "redirect:/login";
            }
            
            task.setUser(user);
            task.setCompleted(false);
            
            // Set default priority if not provided
            if (task.getPriority() == null || task.getPriority().isEmpty()) {
                task.setPriority("MEDIUM");
            }
            
            // Set default due date if not provided
            if (task.getDueDate() == null) {
                task.setDueDate(LocalDateTime.now().plusDays(1));
            }
            
            taskRepository.save(task);
            return "redirect:/tasks";
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error adding task: " + e.getMessage());
            return "redirect:/tasks?error=Failed+to+add+task";
        }
    }

    // Mark as complete/incomplete
    @PostMapping("/{id}/toggle")
    public String toggleComplete(@PathVariable Long id, Principal principal) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null && task.getUser().getEmail().equals(principal.getName())) {
            boolean wasCompleted = task.isCompleted();
            task.setCompleted(!wasCompleted);
            if (task.isCompleted()) {
                task.setCompletedAt(LocalDateTime.now());
            } else {
                task.setCompletedAt(null);
            }
            taskRepository.save(task);

            // Update today's statistic for tasksCompleted
            User user = task.getUser();
            LocalDate today = LocalDate.now();
            Statistic stat = statisticRepository.findByUserAndDate(user, today).orElseGet(() -> {
                Statistic s = new Statistic();
                s.setUser(user);
                s.setDate(today);
                s.setFocusTimeMinutes(0);
                s.setTasksCompleted(0);
                s.setPomodoros(0);
                return s;
            });
            int delta = task.isCompleted() ? 1 : -1;
            stat.setTasksCompleted(Math.max(0, stat.getTasksCompleted() + delta));
            statisticRepository.save(stat);
        }
        return "redirect:/tasks";
    }

    // Delete a task
    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id, Principal principal) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null && task.getUser().getEmail().equals(principal.getName())) {
            taskRepository.delete(task);
        }
        return "redirect:/tasks";
    }

    // Show edit form
    @GetMapping("/{id}/edit")
    public String editTaskForm(@PathVariable Long id, Model model, Principal principal) {
        Task task = taskRepository.findById(id).orElse(null);
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (task == null || !task.getUser().getEmail().equals(principal.getName())) {
            return "redirect:/tasks";
        }
        List<Project> projects = projectRepository.findAllByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("editTask", task);
        model.addAttribute("projects", projects);
        return "edit-task";
    }

    // Handle edit form submission
    @PostMapping("/{id}/edit")
    public String editTask(@PathVariable Long id, @ModelAttribute("editTask") Task updatedTask, Principal principal) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null && task.getUser().getEmail().equals(principal.getName())) {
            task.setTitle(updatedTask.getTitle());
            task.setDescription(updatedTask.getDescription());
            task.setDueDate(updatedTask.getDueDate());
            task.setPriority(updatedTask.getPriority());
            task.setProject(updatedTask.getProject());
            taskRepository.save(task);
        }
        return "redirect:/tasks";
    }
}
