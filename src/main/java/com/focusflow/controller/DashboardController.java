package com.focusflow.controller;

import com.focusflow.model.User;
import com.focusflow.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final NoteRepository noteRepository;
    private final ResourceRepository resourceRepository;
    private final FocusRoomRepository focusRoomRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final MessageRepository messageRepository;

    public DashboardController(UserRepository userRepository,
                               TaskRepository taskRepository,
                               ProjectRepository projectRepository,
                               NoteRepository noteRepository,
                               ResourceRepository resourceRepository,
                               FocusRoomRepository focusRoomRepository,
                               FriendRequestRepository friendRequestRepository,
                               MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.noteRepository = noteRepository;
        this.resourceRepository = resourceRepository;
        this.focusRoomRepository = focusRoomRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        long unfinishedTasks = taskRepository.countByUserAndCompletedFalse(user);
        int projectsCount = projectRepository.findAllByUser(user).size();
        long resourcesCount = resourceRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).size();
        int notesCount = noteRepository.findByUserOrderByIsPinnedDescUpdatedAtDesc(user).size();
        long focusRoomsCount = focusRoomRepository.count();
        
        // Get notifications
        var receivedRequests = friendRequestRepository.findReceivedPendingRequests(user);
        long unreadMessages = messageRepository.countUnreadMessagesForUser(user);

        model.addAttribute("user", user);
        model.addAttribute("unfinishedTasks", unfinishedTasks);
        model.addAttribute("projectsCount", projectsCount);
        model.addAttribute("resourcesCount", resourcesCount);
        model.addAttribute("notesCount", notesCount);
        model.addAttribute("focusRoomsCount", focusRoomsCount);
        model.addAttribute("receivedRequests", receivedRequests);
        model.addAttribute("unreadMessages", unreadMessages);
        return "dashboard";
    }
}


