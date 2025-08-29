package com.focusflow.controller;

import com.focusflow.model.FriendRequest;
import com.focusflow.model.Message;
import com.focusflow.model.User;
import com.focusflow.repository.FriendRequestRepository;
import com.focusflow.repository.MessageRepository;
import com.focusflow.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class FriendsController {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final MessageRepository messageRepository;

    public FriendsController(UserRepository userRepository, 
                           FriendRequestRepository friendRequestRepository,
                           MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/people")
    public String peoplePage(@RequestParam(value = "search", required = false) String search, 
                           Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<User> allUsers;
        
        if (search != null && !search.trim().isEmpty()) {
            // Search by name only
            allUsers = userRepository.findByNameContainingIgnoreCase(search.trim());
        } else {
            // Show all users
            allUsers = userRepository.findAll();
        }
        
        // Filter out current user and apply friend status
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .map(user -> {
                    // Add friend status to user object
                    user.setIsFriend(currentUser.isFriendWith(user));
                    user.setHasPendingRequest(hasPendingRequest(currentUser, user));
                    return user;
                })
                .collect(Collectors.toList());
        
        // Get pending friend requests for notifications
        List<FriendRequest> receivedRequests = friendRequestRepository.findReceivedPendingRequests(currentUser);
        long unreadCount = messageRepository.countUnreadMessagesForUser(currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("users", filteredUsers);
        model.addAttribute("search", search);
        model.addAttribute("receivedRequests", receivedRequests);
        model.addAttribute("unreadCount", unreadCount);
        
        return "people";
    }

    @PostMapping("/people/search")
    @ResponseBody
    public List<User> searchUsers(@RequestParam String query, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
        if (currentUser == null || query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String searchQuery = query.trim();
        
        // Search users by name only
        List<User> searchResults = userRepository.findByNameContainingIgnoreCase(searchQuery);
        
        return searchResults.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .map(user -> {
                    user.setIsFriend(currentUser.isFriendWith(user));
                    user.setHasPendingRequest(hasPendingRequest(currentUser, user));
                    return user;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/people/send-request")
    @ResponseBody
    public String sendFriendRequest(@RequestParam Long receiverId, Principal principal) {
        User sender = userRepository.findByEmail(principal.getName()).orElse(null);
        Optional<User> receiverOpt = userRepository.findById(receiverId);
        
        if (sender == null || receiverOpt.isEmpty()) {
            return "error";
        }
        
        User receiver = receiverOpt.get();
        
        // Check if already friends
        if (sender.isFriendWith(receiver)) {
            return "already_friends";
        }
        
        // Check if request already exists
        if (hasPendingRequest(sender, receiver)) {
            return "request_exists";
        }
        
        // Create friend request
        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        friendRequestRepository.save(request);
        
        return "success";
    }

    @PostMapping("/people/accept-request")
    @ResponseBody
    public String acceptFriendRequest(@RequestParam Long requestId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
        
        if (currentUser == null || requestOpt.isEmpty()) {
            return "error";
        }
        
        FriendRequest request = requestOpt.get();
        
        // Verify the request is for the current user
        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            return "unauthorized";
        }
        
        // Accept the request
        request.setStatus(FriendRequest.FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);
        
        // Add users as friends
        User sender = request.getSender();
        currentUser.addFriend(sender);
        userRepository.save(currentUser);
        userRepository.save(sender);
        
        return "success";
    }

    @PostMapping("/people/reject-request")
    @ResponseBody
    public String rejectFriendRequest(@RequestParam Long requestId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
        
        if (currentUser == null || requestOpt.isEmpty()) {
            return "error";
        }
        
        FriendRequest request = requestOpt.get();
        
        // Verify the request is for the current user
        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            return "unauthorized";
        }
        
        // Reject the request
        request.setStatus(FriendRequest.FriendRequestStatus.REJECTED);
        friendRequestRepository.save(request);
        
        return "success";
    }

    @PostMapping("/people/remove-friend")
    @ResponseBody
    public String removeFriend(@RequestParam Long friendId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
        Optional<User> friendOpt = userRepository.findById(friendId);
        
        if (currentUser == null || friendOpt.isEmpty()) {
            return "error";
        }
        
        User friend = friendOpt.get();
        
        // Remove friendship
        currentUser.removeFriend(friend);
        userRepository.save(currentUser);
        userRepository.save(friend);
        
        return "success";
    }

    @GetMapping("/messages")
    public String messagesPage(Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get all friends
        List<User> friends = currentUser.getFriends().stream().collect(Collectors.toList());
        
        // Get unread message count for each friend
        friends.forEach(friend -> {
            long unreadCount = messageRepository.findConversationBetweenUsers(currentUser, friend)
                    .stream()
                    .filter(message -> message.getReceiver().getId().equals(currentUser.getId()) && !message.isRead())
                    .count();
            friend.setUnreadMessageCount(unreadCount);
        });

        model.addAttribute("user", currentUser);
        model.addAttribute("friends", friends);
        
        return "messages-list";
    }

    @GetMapping("/people/messages/{friendId}")
    public String conversationPage(@PathVariable Long friendId, Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
        Optional<User> friendOpt = userRepository.findById(friendId);
        
        if (currentUser == null || friendOpt.isEmpty()) {
            return "redirect:/people";
        }
        
        User friend = friendOpt.get();
        
        // Check if they are friends
        if (!currentUser.isFriendWith(friend)) {
            return "redirect:/people";
        }
        
        // Get conversation
        List<Message> messages = messageRepository.findConversationBetweenUsers(currentUser, friend);
        
        // Mark messages as read
        messages.stream()
                .filter(message -> message.getReceiver().getId().equals(currentUser.getId()) && !message.isRead())
                .forEach(message -> {
                    message.setRead(true);
                    messageRepository.save(message);
                });
        
        model.addAttribute("user", currentUser);
        model.addAttribute("friend", friend);
        model.addAttribute("messages", messages);
        
        return "messages";
    }

    @PostMapping("/people/send-message")
    @ResponseBody
    public String sendMessage(@RequestParam Long receiverId, @RequestParam String content, Principal principal) {
        User sender = userRepository.findByEmail(principal.getName()).orElse(null);
        Optional<User> receiverOpt = userRepository.findById(receiverId);
        
        if (sender == null || receiverOpt.isEmpty() || content.trim().isEmpty()) {
            return "error";
        }
        
        User receiver = receiverOpt.get();
        
        // Check if they are friends
        if (!sender.isFriendWith(receiver)) {
            return "not_friends";
        }
        
        // Create and save message
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content.trim());
        messageRepository.save(message);
        
        return "success";
    }

    private boolean hasPendingRequest(User user1, User user2) {
        return friendRequestRepository.findPendingRequest(user1, user2).isPresent() ||
               friendRequestRepository.findPendingRequest(user2, user1).isPresent();
    }
}
