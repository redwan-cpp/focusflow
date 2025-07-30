package com.focusflow.controller;

import com.focusflow.model.Task;
import com.focusflow.model.User;
import com.focusflow.repository.TaskRepository;
import com.focusflow.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // List all tasks for the logged-in user
    @GetMapping
    public String tasksPage(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        List<Task> tasks = taskRepository.findAllByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("tasks", tasks);
        model.addAttribute("newTask", new Task());
        return "tasks";
    }

    // Create a new task
    @PostMapping("/add")
    public String addTask(@ModelAttribute("newTask") Task task, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        task.setUser(user);
        if (task.getDueDate() == null) {
            task.setDueDate(LocalDateTime.now().plusDays(1));
        }
        taskRepository.save(task);
        return "redirect:/tasks";
    }

    // Mark as complete/incomplete
    @PostMapping("/{id}/toggle")
    public String toggleComplete(@PathVariable Long id, Principal principal) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null && task.getUser().getEmail().equals(principal.getName())) {
            task.setCompleted(!task.isCompleted());
            taskRepository.save(task);
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
        model.addAttribute("user", user);
        model.addAttribute("editTask", task);
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
            taskRepository.save(task);
        }
        return "redirect:/tasks";
    }
}
