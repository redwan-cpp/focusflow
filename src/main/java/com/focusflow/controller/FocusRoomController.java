package com.focusflow.controller;

import com.focusflow.model.FocusRoom;
import com.focusflow.model.User;
import com.focusflow.repository.FocusRoomRepository;
import com.focusflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/focus-room")
public class FocusRoomController {
    @Autowired
    private FocusRoomRepository focusRoomRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public String focusRoomPage(Model model, Principal principal) {
        List<FocusRoom> rooms = focusRoomRepository.findAll();
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        model.addAttribute("rooms", rooms);
        model.addAttribute("user", user);
        return "focus-room";
    }

    @PostMapping("/create")
    @ResponseBody
    public FocusRoom createRoom(@RequestParam String name, @RequestParam String description) {
        FocusRoom room = new FocusRoom();
        room.setName(name);
        room.setDescription(description);
        room.setActive(true);
        return focusRoomRepository.save(room);
    }

    @PostMapping("/join/{roomId}")
    @ResponseBody
    public String joinRoom(@PathVariable Long roomId, Principal principal) {
        FocusRoom room = focusRoomRepository.findById(roomId).orElse(null);
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (room != null && user != null) {
            room.getParticipants().add(user);
            focusRoomRepository.save(room);
            return "joined";
        }
        return "error";
    }

    @PostMapping("/leave/{roomId}")
    @ResponseBody
    public String leaveRoom(@PathVariable Long roomId, Principal principal) {
        FocusRoom room = focusRoomRepository.findById(roomId).orElse(null);
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (room != null && user != null) {
            room.getParticipants().remove(user);
            focusRoomRepository.save(room);
            return "left";
        }
        return "error";
    }

    @GetMapping("/users/{roomId}")
    @ResponseBody
    public Set<User> getRoomUsers(@PathVariable Long roomId) {
        FocusRoom room = focusRoomRepository.findById(roomId).orElse(null);
        return room != null ? room.getParticipants() : Set.of();
    }
}
